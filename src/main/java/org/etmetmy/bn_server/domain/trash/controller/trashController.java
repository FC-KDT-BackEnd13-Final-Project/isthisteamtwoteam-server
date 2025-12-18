package org.etmetmy.bn_server.domain.trash.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.request.FilePermanentDeleteRequest;
import org.etmetmy.bn_server.domain.file.dto.request.FileRestoreRequest;
import org.etmetmy.bn_server.domain.file.dto.response.FilePermanentDeleteResponse;
import org.etmetmy.bn_server.domain.file.dto.response.FileRestoreResponse;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Trash", description = "휴지통 페이지 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class trashController {

    private final FileService fileService;

    //todo: 4. 삭제된 파일 복원 API
    @Operation(summary = "삭제된 파일 복원", description = "휴지통에 있는 파일을 복원합니다")
    @PatchMapping("/users/projects/{projectId}/files/restore")
    public CommonResponse<FileRestoreResponse> restoreDeletedFiles(
            @PathVariable Long projectId,
            @Valid @RequestBody FileRestoreRequest request,
            HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        FileRestoreResponse response = fileService.restoreDeletedFiles(loginUserId, request);

        return CommonResponse.success("삭제된 파일 복원 성공", response);
    }

    //todo: 5. 삭제된 파일 영구삭제 API
    @Operation(summary = "삭제된 파일 영구삭제", description = "휴지통에 있는 파일을 DB와 S3에서 완전히 삭제합니다")
    @DeleteMapping("/users/projects/{projectId}/files/trash")
    public CommonResponse<FilePermanentDeleteResponse> deleteDeletedFiles(
            @PathVariable Long projectId,
            HttpSession session,
            @Valid @RequestBody FilePermanentDeleteRequest request)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        FilePermanentDeleteResponse response = fileService.hardDeleteFiles(loginUserId, request);

        return CommonResponse.success("삭제된 파일 영구삭제 성공", response);
    }
}

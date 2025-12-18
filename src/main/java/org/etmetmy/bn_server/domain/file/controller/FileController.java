package org.etmetmy.bn_server.domain.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.request.FileDeleteRequest;
import org.etmetmy.bn_server.domain.file.dto.request.FilePermanentDeleteRequest;
import org.etmetmy.bn_server.domain.file.dto.request.FileRestoreRequest;
import org.etmetmy.bn_server.domain.file.dto.response.FilePermanentDeleteResponse;
import org.etmetmy.bn_server.domain.file.dto.response.FileRestoreResponse;
import org.etmetmy.bn_server.domain.file.dto.response.TempFileListDTO;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.post.dto.request.PostPermanentDeleteRequest;
import org.etmetmy.bn_server.domain.post.dto.response.PostPermanentDeleteResponse;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.etmetmy.bn_server.global.aop.HistoryLogger;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "File", description = "파일 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/projects/{projectId}")
public class FileController {

    private final FileService fileService;

    //todo: 1. 임시 파일 업로드 API
    @Operation(summary = "임시 파일 업로드", description = "게시글 작성 시 임시로 파일을 업로드합니다")
    @PostMapping("/posts/files/temp")
    public CommonResponse<List<TempFileListDTO>> postFiles(
            @PathVariable Long projectId,
            @RequestPart("files") List<MultipartFile> files,
            HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        List<TempFileListDTO> response = fileService.postFiles(projectId, files, loginUserId);

        return CommonResponse.success("파일 업로드 성공", response);
    }

    //todo: 2. 임시 파일 삭제 API (hard delete)
    @Operation(summary = "임시 파일 삭제", description = "업로드한 임시 파일을 완전히 삭제합니다")
    @DeleteMapping("files/temp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTempFile(
            @PathVariable Long projectId, @RequestBody FileDeleteRequest request)
    {
        fileService.deleteTempFile(projectId,request.getFileIds());
    }

    //todo: 3. 업로드 된 파일 삭제 API (soft delete)
    @Operation(summary = "게시글 파일 삭제", description = "게시글에 첨부된 파일을 삭제합니다 (soft delete)")
    @DeleteMapping("/posts/{postId}/files/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @HistoryLogger(changeType = ChangeType.DELETE, targetType = "File")
    public void deletePostFiles(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @PathVariable Long fileId,
            HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        fileService.deletePostFiles(projectId, postId, fileId, loginUserId);
    }

    //todo: 4. 삭제된 파일 복원 API
    @Operation(summary = "삭제된 파일 복원", description = "휴지통에 있는 파일을 복원합니다")
    @PatchMapping("/files/restore")
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
    @DeleteMapping("/files/trash")
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

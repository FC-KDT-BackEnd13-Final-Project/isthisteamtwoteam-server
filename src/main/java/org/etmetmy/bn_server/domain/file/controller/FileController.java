package org.etmetmy.bn_server.domain.file.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.request.FileDeleteRequest;
import org.etmetmy.bn_server.domain.file.dto.response.ActiveFileListDTO;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/projects/{projectId}")
public class FileController {

    private final FileService fileService;

    // 1. 프로젝트별 파일 목록 조회 API
    @GetMapping("/files")
    public CommonResponse<List<ActiveFileListDTO>> getFiles(
            @PathVariable Long projectId,
            HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        List<ActiveFileListDTO> fileList = fileService.findAllByProjectId(projectId, session);

        return CommonResponse.success("파일 목록 조회 성공", fileList);
    }

    // 2. 임시 파일 업로드 API
    @PostMapping("/posts/{postId}/files")
    public CommonResponse<List<ActiveFileListDTO>> postFiles(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @RequestPart("files") List<MultipartFile> files)
    {
        List<ActiveFileListDTO> response = fileService.postFiles(projectId, postId, files);

        return CommonResponse.success("파일 업로드 성공", response);
    }

    // 3. 임시 파일 삭제 API (hard delete)
    @DeleteMapping("files")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTempFile(@PathVariable Long projectId, @RequestBody FileDeleteRequest request)
    {
        fileService.deleteFile(projectId,request.getFileId());
    }

    // 4. 업로드 된 파일 삭제 API (soft delete)
    @DeleteMapping("/posts/{postId}/files/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePostFiles(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @PathVariable Long fileId,
            HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        fileService.deletePostFiles(projectId, postId, fileId, loginUserId);
    }
}

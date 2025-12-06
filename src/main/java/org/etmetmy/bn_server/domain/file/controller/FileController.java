package org.etmetmy.bn_server.domain.file.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.ActiveFileListDTO;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/projects/{projectId}")
public class FileController {

    private final FileService fileService;

    @GetMapping("/files")
    public CommonResponse<List<ActiveFileListDTO>> getFiles(
            @PathVariable Long projectId,
            HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        List<ActiveFileListDTO> fileList = fileService.findAllByProjectId(projectId, session);

        return CommonResponse.success("파일 목록 조회 성공", fileList);
    }
}

package org.etmetmy.bn_server.domain.file.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.FileListDTO;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/projects/{projectId}")
public class FileController {

    private final FileService fileService;

    @GetMapping("/files")
    public ResponseEntity<List<FileListDTO>>getFiles(@PathVariable Long projectId) {

        List<FileListDTO> fileList = fileService.findAllByProjectId(projectId);
        return ResponseEntity.ok(fileList);
    }
}

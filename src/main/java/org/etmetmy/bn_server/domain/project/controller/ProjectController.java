package org.etmetmy.bn_server.domain.project.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.entityDto.ProjectDTO;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<Long> createProject(@RequestBody ProjectCreateRequest request) {
        Long currentUserId = getCurrentUserId();   // TODO: Security 연동 시 수정

        Long projectId = projectService.createProject(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(projectId);
    }

    //생성한 프로젝트 저장되어 노출
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(){
        List<ProjectResponse> responses = projectService.getAllProjects();
        return ResponseEntity.ok(responses);
    }

    //임시
    private Long getCurrentUserId() {
        return 1L;
    }
}

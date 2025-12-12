package org.etmetmy.bn_server.domain.project.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/developer/projects")
public class ProjectDeveloperController {

    private final ProjectService projectService;

    // todo: 프로젝트 전체 조회
    @GetMapping
    public CommonResponse<List<ProjectResponse>> getProjects(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        List<ProjectResponse> responses = projectService.getAllProjects(loginUserId);

        return CommonResponse.success("프로젝트 목록조회 성공", responses);
    }

}

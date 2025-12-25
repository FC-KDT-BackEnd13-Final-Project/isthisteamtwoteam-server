package org.etmetmy.bn_server.domain.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectCustomerResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Project (Customer)", description = "고객사 프로젝트 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/projects")
public class ProjectCustomerController {
    private final ProjectService projectService;

    @Operation(summary = "고객사 프로젝트 목록 조회", description = "고객사 사용자가 참여 중인 프로젝트 목록을 조회합니다")
    @GetMapping
    public CommonResponse<List<ProjectCustomerResponse>> getAllProjects(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        List<ProjectCustomerResponse> responses = projectService.getAllProjects(loginUserId);

        return CommonResponse.success("프로젝트 목록조회 성공", responses);

    }

    @Operation(summary = "사용자 참여 프로젝트 목록 조회 (페이지네이션)",
               description = "개발사/고객사 사용자가 참여 중인 모든 프로젝트 목록을 페이지네이션으로 조회합니다 (최신순 정렬)")
    @GetMapping("/page")
    public CommonResponse<Page<ProjectResponse>> getUserProjects(
            HttpSession session,
            @PageableDefault(size = 20) Pageable pageable) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        Page<ProjectResponse> responses = projectService.getUserProjects(loginUserId, pageable);

        return CommonResponse.success("프로젝트 목록 조회 성공", responses);
    }
}

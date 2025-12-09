package org.etmetmy.bn_server.domain.dashboard.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ProjectListResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardResponse;
import org.etmetmy.bn_server.domain.dashboard.service.DashBoardService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/dashboard")
public class DashBoardController {

    private final DashBoardService dashBoardService;

    //todo: 관리자 메인 대시보드 상태 조회 API (STATUS_PENDING인 Post 목록 및 통계)
    @GetMapping
    public CommonResponse<DashBoardResponse> getStatusDashboard(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        DashBoardResponse result = dashBoardService.getStatusDashboard(loginUserId);
        return CommonResponse.success("성공", result);
    }

    //todo: 프로젝트 목록 조회 API
    @GetMapping("/projects")
    public CommonResponse<List<ProjectListResponse>> getProjectList(HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        List<ProjectListResponse> response = dashBoardService.getProjectList(loginUserId);

        return CommonResponse.success("프로젝트 목록 조회 성공", response);
    }
}

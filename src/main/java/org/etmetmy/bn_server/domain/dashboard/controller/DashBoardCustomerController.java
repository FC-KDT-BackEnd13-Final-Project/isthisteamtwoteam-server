package org.etmetmy.bn_server.domain.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ProjectListResponse;
import org.etmetmy.bn_server.domain.dashboard.service.DashBoardService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard (Customer)", description = "고객사 대시보드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/dashboard")
public class DashBoardCustomerController {


    private final DashBoardService dashBoardService;

    // todo : 고객사 기준 대시보드 상태 조회
    @Operation(summary = "고객사 대시보드 조회", description = "고객사 기준으로 대시보드 상태를 조회합니다")
    @GetMapping
    public CommonResponse<DashBoardResponse> getStatusDashboard(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        DashBoardResponse result = dashBoardService.getCustomerStatusDashboard(loginUserId);
        return CommonResponse.success("상태 게시글 조회 성공", result);
    }

    // todo : 고객사 기준 자신이 속한 프로젝트 목록 조회
    @Operation(summary = "고객사 프로젝트 목록 조회", description = "고객사 사용자가 속한 프로젝트 목록을 조회합니다")
    @GetMapping("/projects")
    public CommonResponse<List<ProjectListResponse>> getProjectList(HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        List<ProjectListResponse> response = dashBoardService.getCustomerProjectList(loginUserId);

        return CommonResponse.success("프로젝트 목록 조회 성공", response);
    }
}

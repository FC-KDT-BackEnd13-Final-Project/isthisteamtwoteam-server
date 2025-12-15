package org.etmetmy.bn_server.domain.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ProjectListResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardResponse;
import org.etmetmy.bn_server.domain.dashboard.service.DashBoardService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard (Admin)", description = "관리자 대시보드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/dashboard")
public class DashBoardController {

    private final DashBoardService dashBoardService;

    //todo: 관리자 메인 대시보드 상태 조회 API (STATUS_PENDING인 Post 목록 및 통계)
    @Operation(summary = "관리자 대시보드 조회", description = "관리자 메인 대시보드 상태를 조회합니다 (승인 대기 중인 게시글 및 통계)")
    @GetMapping
    public CommonResponse<DashBoardResponse> getStatusDashboard(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        DashBoardResponse result = dashBoardService.getStatusDashboard(loginUserId);
        return CommonResponse.success("상태 게시글 조회 성공", result);
    }

    //todo: 프로젝트 목록 조회 API
    @Operation(summary = "프로젝트 목록 조회", description = "관리자 대시보드에서 프로젝트 목록을 조회합니다")
    @GetMapping("/projects")
    public CommonResponse<List<ProjectListResponse>> getProjectList(HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        List<ProjectListResponse> response = dashBoardService.getProjectList(loginUserId);

        return CommonResponse.success("프로젝트 목록 조회 성공", response);
    }

    // todo: 승인대기 화면 리스트들 조회
    @Operation(summary = "승인 요청 목록 조회", description = "승인 대기 중인 항목들을 조회합니다")
    @GetMapping("/approval-requests")
    public CommonResponse<ApprovalRequestListResponse> getApprovalRequests(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        ApprovalRequestListResponse result = dashBoardService.getApprovalRequest(loginUserId);

        return CommonResponse.success("승인 요청 알림 조회 성공", result);
    }
}

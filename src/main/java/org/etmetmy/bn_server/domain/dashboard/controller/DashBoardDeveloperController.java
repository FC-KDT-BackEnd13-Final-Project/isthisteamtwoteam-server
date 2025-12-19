package org.etmetmy.bn_server.domain.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.dashboard.service.DashBoardService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard (DEVELOPER)", description = "개발사 대시보드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/developer/dashboard")
public class DashBoardDeveloperController {

    private final DashBoardService dashBoardService;

    //todo: 관리자 메인 대시보드 상태 조회 API (STATUS_PENDING인 Post 목록 및 통계)
    @Operation(summary = "개발사 대시보드 조회", description = "개발사 메인 대시보드 상태를 조회합니다 (승인 대기 중인 게시글 및 통계)")
    @GetMapping
    public CommonResponse<ApprovalRequestListResponse> getStatusDashboard(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        ApprovalRequestListResponse result = dashBoardService.getDeveloperStatusDashboard(loginUserId);
        return CommonResponse.success("상태 게시글 조회 성공", result);
    }
}

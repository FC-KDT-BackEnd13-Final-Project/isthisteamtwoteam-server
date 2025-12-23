package org.etmetmy.bn_server.domain.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardResponse;
import org.etmetmy.bn_server.domain.dashboard.service.DashBoardService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard (DEVELOPER)", description = "개발사 대시보드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/developer/dashboard")
public class DashBoardDeveloperController {

    private final DashBoardService dashBoardService;

    //todo: 개발사 메인 대시보드 상태 조회 API (승인대기 게시글, 반려 게시글, 진행중 프로젝트, 유지보수 프로젝트 갯수 및 목록 조회)
    @Operation(summary = "개발사 대시보드 조회", description = "승인대기 게시글, 반려 게시글, 진행중 프로젝트, 유지보수 프로젝트 갯수 및 목록 조회")
    @GetMapping
    public CommonResponse<DashBoardResponse> getStatusDashboard(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        DashBoardResponse result = dashBoardService.getDeveloperStatusDashboard(loginUserId);
        return CommonResponse.success("개발사 대시보드 상태 조회 성공", result);
    }
}

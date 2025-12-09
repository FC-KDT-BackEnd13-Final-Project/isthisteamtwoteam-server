package org.etmetmy.bn_server.domain.dashboard.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.service.DashBoardService;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/dashboard")
public class DashBoardController {

    private final DashBoardService dashBoardService;

    // 관리자 메인대시보드 상태 조회
    @GetMapping
    public void getStatusDashboard(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        dashBoardService.getStatusDashboard(loginUserId);
    }
}

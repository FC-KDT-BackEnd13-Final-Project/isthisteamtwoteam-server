package org.etmetmy.bn_server.domain.dashboard.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardResponse;
import org.etmetmy.bn_server.domain.dashboard.service.DashBoardService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/dashboard")
public class DashBoardCustomerController {


    private final DashBoardService dashBoardService;

    @GetMapping
    public CommonResponse<DashBoardResponse> getStatusDashboard(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        DashBoardResponse result = dashBoardService.getCustomerStatusDashboard(loginUserId);
        return CommonResponse.success("상태 게시글 조회 성공", result);
    }


}

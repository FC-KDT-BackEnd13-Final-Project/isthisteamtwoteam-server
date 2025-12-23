package org.etmetmy.bn_server.domain.request.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.request.dto.ApprovalNotiResponse;
import org.etmetmy.bn_server.domain.request.service.RequestService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Request", description = "승인 요청 알림 API")
@RestController
@RequestMapping("/api/v1/admin/projects")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    // todo: 개별 프로젝트 승인대기 화면 리스트들 조회
    @Operation(summary = "개별 프로젝트 승인 요청 목록 조회", description = "개별 프로젝트 게시글 request 항목들을 조회합니다")
    @GetMapping("/{projectId}/approval-requests")
    public CommonResponse<ApprovalNotiResponse> getApprovalRequests(
            @PathVariable Long projectId, HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        ApprovalNotiResponse result = requestService.getApprovalRequest(projectId, loginUserId);

        return CommonResponse.success("승인 요청 알림 조회 성공", result);
    }

    // todo: 전체 프로젝트 승인대기 화면 리스트들 조회
    @Operation(summary = "전체 프로젝트 승인 요청 목록 조회", description = "전체 프로젝트 승인 대기 중인 항목들을 조회합니다")
    @GetMapping("/approval-requests")
    public CommonResponse<ApprovalRequestListResponse> getTotalApprovalRequests(HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        ApprovalRequestListResponse result = requestService.getAdminApprovalRequest(loginUserId);

        return CommonResponse.success("승인 요청 알림 조회 성공", result);
    }
}

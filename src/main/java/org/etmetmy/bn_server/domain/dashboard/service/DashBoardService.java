package org.etmetmy.bn_server.domain.dashboard.service;

import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ProjectListResponse;
import java.util.List;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardResponse;

public interface DashBoardService {

    // 프로젝트 목록 조회 (관리자용: 모든 프로젝트)
    List<ProjectListResponse> getProjectList(Long loginUserId);

    // 고객용 프로젝트 목록 조회 (고객이 속한 프로젝트만)
    List<ProjectListResponse> getCustomerProjectList(Long loginUserId);

    DashBoardResponse getStatusDashboard(Long loginUserId);

    // 고객용 대시보드: 고객이 참여 중인 프로젝트만 필터링
    DashBoardResponse getCustomerStatusDashboard(Long loginUserId);

    ApprovalRequestListResponse getApprovalRequest(Long loginUserId);

    DashBoardResponse getDeveloperStatusDashboard(Long loginUserId);
}

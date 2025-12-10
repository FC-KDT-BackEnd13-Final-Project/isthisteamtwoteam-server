package org.etmetmy.bn_server.domain.dashboard.service;

import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ProjectListResponse;
import java.util.List;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardResponse;

public interface DashBoardService {

    // 프로젝트 목록 조회
    List<ProjectListResponse> getProjectList(Long loginUserId);

    DashBoardResponse getStatusDashboard(Long loginUserId);

    ApprovalRequestListResponse getApprovalRequest();
}

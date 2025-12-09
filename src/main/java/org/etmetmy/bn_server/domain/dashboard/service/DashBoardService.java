package org.etmetmy.bn_server.domain.dashboard.service;

import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardResponse;

public interface DashBoardService {
    DashBoardResponse getStatusDashboard(Long loginUserId);
}

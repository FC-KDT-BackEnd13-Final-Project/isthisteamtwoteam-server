package org.etmetmy.bn_server.domain.dashboard.service;

import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardStatusResponseDTO;

import java.util.List;

public interface DashBoardService {
    List<DashBoardStatusResponseDTO> getStatusDashboard(Long loginUserId);
}

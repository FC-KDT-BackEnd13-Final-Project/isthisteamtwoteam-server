package org.etmetmy.bn_server.domain.request.service;

import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.request.dto.ApprovalNotiResponse;


public interface RequestService {

    ApprovalNotiResponse getApprovalRequest(Long projectId, Long loginUserId);

    ApprovalRequestListResponse getAdminApprovalRequest(Long loginUserId);
}

package org.etmetmy.bn_server.domain.request.service;

import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.request.dto.ApprovalNotiResponse;


public interface RequestService {

    ApprovalRequestListResponse getApprovalRequest(Long projectId, Long loginUserId);

    ApprovalRequestListResponse getMyApprovalRequest(Long loginUserId);

    ApprovalRequestListResponse getAdminApprovalRequest(Long loginUserId);
}

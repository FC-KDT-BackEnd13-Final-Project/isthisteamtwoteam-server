package org.etmetmy.bn_server.domain.activityLog.service;

import org.etmetmy.bn_server.domain.activityLog.dto.ActivityLogResponse;

import java.util.List;

public interface ActivityLogService {
    List<ActivityLogResponse> getProjectLogs(Long projectId);
}

package org.etmetmy.bn_server.domain.activityLog.service;

import org.etmetmy.bn_server.domain.activityLog.dto.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.dto.LogDetail;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;

import java.util.List;

public interface ActivityLogService {
    List<ActivityLogResponse> getProjectLogs(Long projectId);
    void saveLog(Long projectId, Long userId, ActivityAction action,
                 String targetType, Long targetId, List<LogDetail> details, String ipAddress);
}

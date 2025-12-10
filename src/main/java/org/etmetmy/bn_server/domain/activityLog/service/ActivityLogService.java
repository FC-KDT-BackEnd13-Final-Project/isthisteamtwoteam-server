package org.etmetmy.bn_server.domain.activityLog.service;

import org.etmetmy.bn_server.domain.activityLog.dto.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;

import java.util.List;

public interface ActivityLogService {
    /**
     * 활동 로그 저장
     */
    void saveLog(Long projectId, String projectName, Long userId, String userName,
                 ActivityAction action, String targetType, Long targetId,
                 String ipAddress, String description);

    /**
     * 프로젝트별 활동 로그 조회
     */
    List<ActivityLogResponse> getProjectLogs(Long projectId);
}

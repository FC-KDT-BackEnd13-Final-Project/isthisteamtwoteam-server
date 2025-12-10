package org.etmetmy.bn_server.domain.activityLog.event;

import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;


public record ActivityLogEvent(
        Long projectId,
        String projectName,
        Long userId,
        String userName,
        ActivityAction action, // AOP에서 String을 Enum으로 변환하여 저장
        String targetType,
        Long targetId,
        String ipAddress,
        String description
) {}
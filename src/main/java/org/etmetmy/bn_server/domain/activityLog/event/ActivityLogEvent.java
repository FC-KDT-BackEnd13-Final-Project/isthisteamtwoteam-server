package org.etmetmy.bn_server.domain.activityLog.event;

import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;

public record ActivityLogEvent(
        Long projectId,
        Long userId, // String userName 제거
        ActivityAction action,
        String targetType,
        Long targetId,
        String ipAddress
) {}
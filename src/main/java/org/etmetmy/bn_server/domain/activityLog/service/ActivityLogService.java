package org.etmetmy.bn_server.domain.activityLog.service;

import org.etmetmy.bn_server.domain.activityLog.dto.request.ActivityLogCreateRequest;
import org.etmetmy.bn_server.domain.activityLog.dto.response.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ActivityLogService {

    void saveLog(ActivityLogCreateRequest request);

    Page<ActivityLogResponse> getProjectLogsWithFilters(
            Long projectId,
            ActivityAction action,
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<ActivityLogResponse> getTargetLogs(
            Long projectId,
            String targetType,
            Long targetId,
            Pageable pageable
    );

    Page<ActivityLogResponse> getAllLogsWithPaging(Pageable pageable);
}
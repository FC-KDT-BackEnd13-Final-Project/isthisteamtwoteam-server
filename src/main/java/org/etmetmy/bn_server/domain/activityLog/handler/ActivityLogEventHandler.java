package org.etmetmy.bn_server.domain.activityLog.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.activityLog.dto.request.ActivityLogCreateRequest;
import org.etmetmy.bn_server.domain.activityLog.event.ActivityLogEvent;
import org.etmetmy.bn_server.domain.activityLog.service.ActivityLogService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityLogEventHandler {

    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;
    @Async
    @EventListener
    public void handle(ActivityLogEvent event) {
        try {
            log.debug("ActivityLogEvent 수신: TargetType={}, TargetId={}",
                    event.targetType(), event.targetId());

            ActivityLogCreateRequest request = ActivityLogCreateRequest.builder()
                    .projectId(event.projectId())
                    .userId(event.userId())
                    .action(event.action())
                    .targetType(event.targetType())
                    .targetId(event.targetId())
                    .ipAddress(event.ipAddress())
                    .detail(event.detail())
                    .build();

            activityLogService.saveLog(request);

            log.debug("Activity Log 저장 완료: TargetType={}, TargetId={}",
                    event.targetType(), event.targetId());

        } catch (Exception e) {
            log.error("ActivityLog 저장 중 오류 발생: TargetType={}, TargetId={}",
                    event.targetType(), event.targetId(), e);
        }
    }
}
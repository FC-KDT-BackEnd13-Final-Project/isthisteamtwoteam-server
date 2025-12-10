package org.etmetmy.bn_server.domain.activityLog.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.event.ActivityLogEvent;
import org.etmetmy.bn_server.domain.activityLog.repository.ActivityLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityLogEventHandler {

    private final ActivityLogRepository activityLogRepository;

    /**
     * ActivityLogEvent를 수신하여 비동기적으로 로그를 DB에 저장
     * 트랜잭션 커밋 후에 실행됨
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ActivityLogEvent event) {
        try {
            ActivityLog activityLog = ActivityLog.builder()
                    .projectId(event.projectId())
                    .projectName(event.projectName())
                    .userId(event.userId())
                    .userName(event.userName())
                    .action(event.action())
                    .targetType(event.targetType())
                    .targetId(event.targetId())
                    .ipAddress(event.ipAddress())
                    .description(event.description())
                    .build();

            activityLogRepository.save(activityLog);

            log.info("Activity Log 저장 완료: {}", event.description());

        } catch (Exception e) {
            log.error("ActivityLog 저장 중 오류 발생: {}", event.description(), e);
        }
    }
}
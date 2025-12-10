package org.etmetmy.bn_server.domain.activityLog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.activityLog.dto.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.activityLog.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional
    public void saveLog(Long projectId, String projectName, Long userId, String userName,
                        ActivityAction action, String targetType, Long targetId,
                        String ipAddress, String description) {
        try {
            ActivityLog logEntity = ActivityLog.builder()
                    .projectId(projectId)
                    .projectName(projectName)
                    .userId(userId)
                    .userName(userName)
                    .action(action)
                    .targetType(targetType)
                    .targetId(targetId)
                    .ipAddress(ipAddress)
                    .description(description)
                    .build();

            activityLogRepository.save(logEntity);

            log.debug("활동 로그 저장 완료: {}", description);

        } catch (Exception e) {
            log.error("활동 로그 저장 실패: projectId={}, targetId={}, error={}",
                    projectId, targetId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getProjectLogs(Long projectId) {
        List<ActivityLog> logs = activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId);

        return logs.stream()
                .map(ActivityLogResponse.Converter::from)
                .toList();
    }
}
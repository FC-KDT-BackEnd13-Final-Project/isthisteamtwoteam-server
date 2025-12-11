package org.etmetmy.bn_server.domain.activityLog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.activityLog.dto.request.ActivityLogCreateRequest;
import org.etmetmy.bn_server.domain.activityLog.dto.response.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.activityLog.repository.ActivityLogRepository;
import org.etmetmy.bn_server.domain.activityLog.util.ActivityDescriptionGenerator;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ActivityDescriptionGenerator descriptionGenerator;

    @Override
    @Transactional
    public void saveLog(ActivityLogCreateRequest request) {
        ActivityLog logEntity = ActivityLogCreateRequest.Converter.toEntityWithRepositories(
                request,
                userRepository,
                projectRepository,
                descriptionGenerator
        );

        try {
            activityLogRepository.save(logEntity);
            log.debug("활동 로그 저장 완료: {}", logEntity.getDescription());
        } catch (Exception e) {
            log.error("활동 로그 저장 실패: projectId={}, targetId={}, error={}",
                    request.projectId(), request.targetId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getProjectLogsWithPaging(Long projectId, Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findByProjectId(projectId, pageable);
        return logs.map(ActivityLogResponse.Converter::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getProjectLogsByAction(Long projectId, ActivityAction action) {
        List<ActivityLog> logs = activityLogRepository.findByProjectIdAndActionOrderByCreatedAtDesc(projectId, action);
        return ActivityLogResponse.Converter.fromList(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getProjectLogsByUser(Long projectId, Long userId) {
        List<ActivityLog> logs = activityLogRepository.findByProjectIdAndUserIdOrderByCreatedAtDesc(projectId, userId);
        return ActivityLogResponse.Converter.fromList(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getProjectLogsByDateRange(
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<ActivityLog> logs = activityLogRepository.findByProjectIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                projectId, startDate, endDate
        );
        return ActivityLogResponse.Converter.fromList(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getAllLogsWithPaging(Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findAll(pageable);
        return logs.map(ActivityLogResponse.Converter::from);
    }
}
package org.etmetmy.bn_server.domain.activityLog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.activityLog.dto.request.ActivityLogCreateRequest;
import org.etmetmy.bn_server.domain.activityLog.dto.response.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.activityLog.repository.ActivityLogRepository;
import org.etmetmy.bn_server.domain.activityLog.util.ActivityDescriptionGenerator;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        try {
            User user = (request.userId() != null)
                    ? userRepository.findById(request.userId()).orElse(null) : null;

            // 방어 로직: projectId가 null이면 조회를 건너뜀
            Project project = (request.projectId() != null)
                    ? projectRepository.findById(request.projectId()).orElse(null) : null;

            String description = descriptionGenerator.generate(
                    request.action(),
                    request.targetType(),
                    request.targetId(),
                    user,
                    project,
                    request.detail()
            );

            ActivityLog logEntity = ActivityLogCreateRequest.Converter.toEntity(
                    request,
                    user,
                    project,
                    description
            );

            activityLogRepository.save(logEntity);
            log.info("활동 로그 저장 완료: {}", logEntity.getDescription());

        } catch (Exception e) {
            log.error("활동 로그 저장 실패: projectId={}, targetType={}, targetId={}",
                    request.projectId(), request.targetType(), request.targetId(), e);
            // 로그 저장 실패가 비즈니스 로직에 영향을 주지 않도록 예외를 삼킴
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getProjectLogsWithFilters(
            Long projectId,
            ActivityAction action,
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Page<ActivityLog> logs = activityLogRepository.findByProjectIdWithFilters(
                projectId, action, userId, startDate, endDate, pageable
        );
        return logs.map(ActivityLogResponse.Converter::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getTargetLogs(
            Long projectId,
            String targetType,
            Long targetId,
            Pageable pageable
    ) {
        Page<ActivityLog> logs = activityLogRepository
                .findByProjectIdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(
                        projectId, targetType, targetId, pageable
                );
        return logs.map(ActivityLogResponse.Converter::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getAllLogsWithPaging(Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findAll(pageable);
        return logs.map(ActivityLogResponse.Converter::from);
    }
}
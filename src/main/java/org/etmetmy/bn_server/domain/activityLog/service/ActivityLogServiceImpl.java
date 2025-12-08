package org.etmetmy.bn_server.domain.activityLog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;

import org.etmetmy.bn_server.domain.activityLog.dto.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.dto.LogDetail;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.activityLog.repository.ActivityLogRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j // 로그 출력을 위해 추가
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // JSON 변환기

    /**
     * 활동 로그를 저장. ID 기반으로 로그 엔티티를 생성.
     */
    @Transactional
    public void saveLog(Long projectId, Long userId, ActivityAction action,
                        String targetType, Long targetId, List<LogDetail> details,
                        String ipAddress) {

        // 1. Project 및 User 유효성 검증
        projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(StatusCode.PROJECT_NOT_FOUND));

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(StatusCode.USER_NOT_FOUND));

        // 2. JSON 변환
        String jsonDetail = null;
        try {
            if (details != null && !details.isEmpty()) {
                jsonDetail = objectMapper.writeValueAsString(details);
            }
        } catch (JsonProcessingException e) {
            log.error(">>> 로그 상세 JSON 변환 실패", e);
        }

        // 3. 엔티티 생성 및 저장
        ActivityLog logEntity = ActivityLog.builder()
                .projectId(projectId)
                .userId(userId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(jsonDetail)
                .ipAddress(ipAddress)
                .build();

        activityLogRepository.save(logEntity);
    }

    /**
     * 특정 프로젝트의 활동 로그를 조회하고 DTO로 변환
     */
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getProjectLogs(Long projectId) {

        // 프로젝트 유효성 검증 (조회 시에도 유효성 확인)
        projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(StatusCode.PROJECT_NOT_FOUND));

        // DB에서 최신순으로 가져오기
        List<ActivityLog> logs = activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId);

        // 엔티티 -> DTO 변환 (JSON 파싱 포함)
        return logs.stream().map(entity -> {
            List<LogDetail> parsedDetails = new ArrayList<>();

            if (entity.getDetail() != null && !entity.getDetail().isEmpty()) {
                try {
                    parsedDetails = objectMapper.readValue(
                            entity.getDetail(),
                            new TypeReference<List<LogDetail>>() {
                            }
                    );
                } catch (JsonProcessingException e) {
                    log.error("로그 상세 파싱 실패 logId={}", entity.getLogId(), e);
                }
            }
            return ActivityLogResponse.Converter.from(entity, parsedDetails);
        }).toList();
    }
}
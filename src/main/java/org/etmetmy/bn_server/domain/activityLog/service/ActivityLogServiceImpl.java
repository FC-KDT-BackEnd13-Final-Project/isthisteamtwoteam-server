package org.etmetmy.bn_server.domain.activityLog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.activityLog.dto.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.dto.LogDetail;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.activityLog.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j // 로그 출력을 위해 추가
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper; // JSON 변환기

    @Transactional
    public void saveLog(Long projectId, Long userId, ActivityAction action,
                        String targetType, Long targetId, List<LogDetail> details) {

        // 1. JSON 변환 (내 담당 로직 테스트)
        String jsonDetail = null;
        try {
            if (details != null && !details.isEmpty()) {
                jsonDetail = objectMapper.writeValueAsString(details);
            }
        } catch (JsonProcessingException e) {
            log.error(">>> JSON 변환 실패", e);
        }

        // 2. 엔티티 생성 (Project 객체 조회 없이 바로 ID 저장)
        ActivityLog logEntity = ActivityLog.builder()
                .projectId(projectId) // 👈 객체 대신 숫자(ID)를 바로 넣음
                .userId(userId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(jsonDetail)
                .ipAddress("127.0.0.1")
                .build();

        // 3. 저장
        activityLogRepository.save(logEntity);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getProjectLogs(Long projectId) {
        // 1. DB에서 최신순으로 가져오기
        List<ActivityLog> logs = activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId);

        // 2. 엔티티 -> DTO 변환 (JSON 파싱 포함)
        return logs.stream().map(entity-> {
            List<LogDetail> parsedDetails = new ArrayList<>();

            // JSON String -> List<LogDetail> 변환 로직
            if (entity.getDetail() != null && !entity.getDetail().isEmpty()) {
                try {
                    parsedDetails = objectMapper.readValue(
                            entity.getDetail(),
                            new TypeReference<List<LogDetail>>() {} // 리스트 타입으로 변환
                    );
                } catch (JsonProcessingException e) {
                    // 파싱 실패해도 전체 로직이 죽으면 안 되니까 빈 리스트 반환하고 로그만 남김
                    log.error("로그 상세 파싱 실패 logId={}", entity.getLogId(), e);
                }
            }
            return ActivityLogResponse.Converter.from(entity, parsedDetails);
        }).toList();
    }
}
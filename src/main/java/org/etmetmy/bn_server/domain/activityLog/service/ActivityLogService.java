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
//import org.etmetmy.bn_server.domain.project.entity.Project;
//import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j // 로그 출력을 위해 추가
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    //private final ProjectRepository projectRepository; // 팀원 파트 (없으면 빨간줄 뜰 수 있음)
    private final ObjectMapper objectMapper; // JSON 변환기

    @Transactional
    public void saveLog(Long projectId, Long userId, ActivityAction action,
                        String targetType, Long targetId, List<LogDetail> details) {

        log.info("==== [ActivityLogService] 로그 저장 요청 시작 ====");

        // 1. JSON 변환 (내 담당 로직 테스트)
        String jsonDetail = null;
        try {
            if (details != null && !details.isEmpty()) {
                jsonDetail = objectMapper.writeValueAsString(details);
                log.info(">>> JSON 변환 성공: {}", jsonDetail);
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
        log.info("==== [테스트 모드] DB 저장 완료 (Project ID: {}) ====", projectId);
    }

        /* 2. 프로젝트 조회
        Project project = null;
        try {
            // 팀원이 Repository를 아직 안 만들었거나 DB에 데이터가 없으면 여기서 에러가 남
            project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.warn("!!! 팀원 파트(Project) 문제로 DB 조회를 건너뜁니다. (임시 테스트 모드) !!!");
            log.warn("에러 내용: {}", e.getMessage());

            // 테스트를 위해 저장을 안 하고 여기서 메서드를 끝냅니다.
            // (DB에 넣으려면 Project 객체가 필수라서 어쩔 수 없음)
            return;
        }

        // 3. 로그 엔티티 생성 및 저장
        ActivityLog logEntity = ActivityLog.builder()
                .project(project)
                .userId(userId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(jsonDetail)
                .ipAddress("127.0.0.1") // 임시 IP
                .build();

        activityLogRepository.save(logEntity);
        log.info("==== [ActivityLogService] DB 저장 완료 ====");
     }
         */

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

            return ActivityLogResponse.from(entity, parsedDetails);
        }).toList();
    }
}
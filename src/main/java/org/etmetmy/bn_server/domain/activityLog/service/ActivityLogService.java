package org.etmetmy.bn_server.domain.activityLog.service;

import org.etmetmy.bn_server.domain.activityLog.dto.request.ActivityLogCreateRequest;
import org.etmetmy.bn_server.domain.activityLog.dto.response.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogService {

    /**
     * 활동 로그 저장
     */
    void saveLog(ActivityLogCreateRequest request);

    /**
     * 프로젝트의 활동 로그 조회 (페이지네이션, 최신순)
     */
    Page<ActivityLogResponse> getProjectLogsWithPaging(Long projectId, Pageable pageable);

    /**
     * 프로젝트의 활동 로그 조회 (액션 타입 필터링)
     */
    List<ActivityLogResponse> getProjectLogsByAction(Long projectId, ActivityAction action);

    /**
     * 프로젝트의 활동 로그 조회 (사용자 필터링)
     */
    List<ActivityLogResponse> getProjectLogsByUser(Long projectId, Long userId);

    /**
     * 프로젝트의 활동 로그 조회 (기간 필터링)
     */
    List<ActivityLogResponse> getProjectLogsByDateRange(Long projectId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 전체 활동 로그 조회 (관리자용)
     */
    Page<ActivityLogResponse> getAllLogsWithPaging(Pageable pageable);
}
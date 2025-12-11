package org.etmetmy.bn_server.domain.activityLog.repository;

import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * 최신순
     */
    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    /**
     * 페이지네이션
     */
    Page<ActivityLog> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 프로젝트별 + 액션 타입별 활동 로그 조회
     */
    List<ActivityLog> findByProjectIdAndActionOrderByCreatedAtDesc(Long projectId, ActivityAction action);

    /**
     * 프로젝트별 + 사용자별 활동 로그 조회
     */
    List<ActivityLog> findByProjectIdAndUserIdOrderByCreatedAtDesc(Long projectId, Long userId);

    /**
     * 프로젝트별 + 기간별 활동 로그 조회
     */
    List<ActivityLog> findByProjectIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 전체 활동 로그 조회 (페이지네이션)
     */
    Page<ActivityLog> findAll(Pageable pageable);
}
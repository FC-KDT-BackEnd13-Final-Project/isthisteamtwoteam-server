package org.etmetmy.bn_server.domain.activityLog.repository;

import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // 페이지네이션 (기본 정렬: createdAt DESC)
    @Query("SELECT a FROM ActivityLog a WHERE a.projectId = :projectId ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    // 액션별 필터링
    List<ActivityLog> findByProjectIdAndActionOrderByCreatedAtDesc(Long projectId, ActivityAction action);

    // 사용자별 필터링
    List<ActivityLog> findByProjectIdAndUserIdOrderByCreatedAtDesc(Long projectId, Long userId);

    // 기간별 필터링
    List<ActivityLog> findByProjectIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 관리자용 (전체 조회)
    @Query("SELECT a FROM ActivityLog a ORDER BY a.createdAt DESC")
    Page<ActivityLog> findAll(Pageable pageable);
}
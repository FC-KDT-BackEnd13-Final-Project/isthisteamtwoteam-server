package org.etmetmy.bn_server.domain.activityLog.repository;

import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // 기본 페이징 조회
    Page<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    // 특정 대상(게시글, 체크리스트 등)의 모든 이력 조회
    Page<ActivityLog> findByProjectIdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(
            Long projectId, String targetType, Long targetId, Pageable pageable
    );

    //필터링 조회
    @Query("SELECT al FROM ActivityLog al " +
            "WHERE al.projectId = :projectId " +
            "AND (:action IS NULL OR al.action = :action) " +
            "AND (:userId IS NULL OR al.userId = :userId) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR al.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR al.createdAt <= :endDate)")
    Page<ActivityLog> findByProjectIdWithFilters(
            @Param("projectId") Long projectId,
            @Param("action") ActivityAction action,
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
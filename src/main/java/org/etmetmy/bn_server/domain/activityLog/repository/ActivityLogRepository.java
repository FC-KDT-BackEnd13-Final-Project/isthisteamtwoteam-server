package org.etmetmy.bn_server.domain.activityLog.repository;

import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // 특정 프로젝트의 로그 조회 (최신순)
    //List<ActivityLog> findByProject_ProjectIdOrderByCreatedAtDesc(Long projectId);
    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    // 특정 타겟(예: 특정 게시글)의 로그 조회
    List<ActivityLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);
}
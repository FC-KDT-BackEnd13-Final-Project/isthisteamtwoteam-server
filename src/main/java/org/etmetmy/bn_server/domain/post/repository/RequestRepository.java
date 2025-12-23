package org.etmetmy.bn_server.domain.post.repository;

import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByPostPostIdAndApproveStatus(Long postId, RequestStatus status);

    /**
     * 여러 Post의 특정 상태 Request 일괄 조회
     */
    @Query("SELECT r FROM Request r WHERE r.post.postId IN :postIds AND r.approveStatus = :status")
    List<Request> findByPostIdsAndApproveStatus(@Param("postIds") List<Long> postIds, @Param("status") RequestStatus status);

    @Query("SELECT r FROM Request r WHERE r.post.postId = :postId")
    Request findByPostId(@Param("postId") Long postId);

    // 여러 Post ID로 Request 일괄 조회 (모든 상태 포함, N+1 문제 방지)
    @Query("SELECT r FROM Request r WHERE r.post.postId IN :postIds")
    List<Request> findByPostPostIdIn(@Param("postIds") List<Long> postIds);

    @Query("""
    SELECT r.approveStatus, COUNT(r)
    FROM Request r
    JOIN r.post p
    WHERE p.project.id IN :projectIds
    GROUP BY r.approveStatus
    """)
    List<Object[]> countByStatus(@Param("projectIds") List<Long> projectIds);
}
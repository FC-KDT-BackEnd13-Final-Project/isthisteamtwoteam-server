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
}
package org.etmetmy.bn_server.domain.post.repository;

import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByPostPostIdAndApproveStatus(Long postId, RequestStatus status);
}
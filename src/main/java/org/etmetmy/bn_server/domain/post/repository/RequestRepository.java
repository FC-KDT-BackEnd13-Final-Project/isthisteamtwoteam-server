package org.etmetmy.bn_server.domain.post.repository;

import org.etmetmy.bn_server.domain.post.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RequestRepository extends JpaRepository<Request, String> {
    Optional<Request> findByPostPostIdAndApproveStatus(Long postId, String status);
}
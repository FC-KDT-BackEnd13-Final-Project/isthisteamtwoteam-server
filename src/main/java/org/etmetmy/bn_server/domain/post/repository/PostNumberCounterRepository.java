package org.etmetmy.bn_server.domain.post.repository;

import org.etmetmy.bn_server.domain.post.entity.PostNumberCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostNumberCounterRepository extends JpaRepository<PostNumberCounter, Long> {

    /**
     * 프로젝트 ID로 PostNumberCounter 조회
     */
    Optional<PostNumberCounter> findByProjectId(Long projectId);
}


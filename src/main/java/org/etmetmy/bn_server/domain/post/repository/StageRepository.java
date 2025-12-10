package org.etmetmy.bn_server.domain.post.repository;

import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.post.entity.StageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StageRepository extends JpaRepository<Stage, Long> {
    Optional<Stage> findByStageType(StageType stageType);
}

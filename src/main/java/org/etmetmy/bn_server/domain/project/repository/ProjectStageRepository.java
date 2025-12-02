package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectStageRepository extends JpaRepository<Stage, Long> {
    Optional<Stage> findByStageName(String stageName);

    @Query("SELECT s FROM Stage s WHERE LOWER(REPLACE(s.stageName, ' ', '')) = LOWER(REPLACE(:stageName, ' ', ''))")
    Optional<Stage> findByStageNameNormalized(@Param("stageName") String stageName);
}

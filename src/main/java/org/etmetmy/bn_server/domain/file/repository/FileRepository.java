package org.etmetmy.bn_server.domain.file.repository;

import org.etmetmy.bn_server.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query(value = "SELECT f.* FROM file f " +
                   "JOIN post p ON f.post_id = p.post_id " +
                   "WHERE p.project_id = :projectId " +
                   "AND f.is_deleted = false",
           nativeQuery = true)
    List<File> findByProjectId(@Param("projectId") Long projectId);
}

package org.etmetmy.bn_server.domain.file.repository;

import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query("SELECT f FROM File f " +
           "JOIN FETCH f.post p " +
           "WHERE p.project.id = :projectId " +
           "AND f.isDeleted = false")
    List<File> findByProjectId(@Param("projectId") Long projectId);

    List<File> findByPost(Post post);
}

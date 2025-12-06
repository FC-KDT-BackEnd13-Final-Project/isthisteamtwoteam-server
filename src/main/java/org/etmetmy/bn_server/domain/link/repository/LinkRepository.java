package org.etmetmy.bn_server.domain.link.repository;

import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Long> {

    @Query("SELECT l FROM Link l " +
           "JOIN FETCH l.post p " +
           "WHERE p.project.id = :projectId " +
           "AND l.isDeleted = false")
    List<Link> findByProjectId(@Param("projectId") Long projectId);

    List<Link> findByPost(Post post);
}
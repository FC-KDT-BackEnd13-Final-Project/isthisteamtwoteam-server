package org.etmetmy.bn_server.domain.post.repository;

import jakarta.persistence.LockModeType;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, Long> {


    @Query("select p from Post p " +
            "join fetch p.user u " +
            "where p.postId = :postId")
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);

    // 전체 게시글 조회 (User + Stage 함께 조회)
    @Query("select p from Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "where p.project.id = :projectId " +
            "order by p.postNumber desc")
    // ← postNumber로 정렬
    List<Post> findAllByProjectId(@Param("projectId") Long projectId);

    // 완료된 게시글 조회
    @Query("select p from Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "where p.project.id = :projectId " +
            "and p.isCompleted = true " +
            "order by p.postNumber desc")
    List<Post> findCompletedByProjectId(@Param("projectId") Long projectId);

    // 미완료 게시글 조회
    @Query("select p from Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "where p.project.id = :projectId " +
            "and p.isCompleted = false " +
            "order by p.postNumber desc ")
    List<Post> findUncompletedByProjectId(@Param("projectId") Long projectId);

    /**
     * 프로젝트 내 최대 게시글 번호 조회
     */
    @Query("SELECT MAX(p.postNumber) FROM Post p WHERE p.project.id = :projectId")
    Optional<Long> findMaxPostNumberByProjectId(@Param("projectId") Long projectId);

}
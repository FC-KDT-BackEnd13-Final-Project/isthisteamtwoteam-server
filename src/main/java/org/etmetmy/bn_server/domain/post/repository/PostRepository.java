package org.etmetmy.bn_server.domain.post.repository;

import jakarta.persistence.LockModeType;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시글 조회 (files 포함)
    @Query("select distinct p from Post p " +
            "left join fetch p.files " +
            "where p.postId = :postId")
    Optional<Post> findByIdWithFiles(@Param("postId") Long postId);

    // 게시글 조회 (links 포함)
    @Query("select distinct p from Post p " +
            "left join fetch p.links " +
            "where p.postId = :postId")
    Optional<Post> findByIdWithLinks(@Param("postId") Long postId);

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

    @Query("select p from Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "where p.project.id = :projectId " +
            "and p.stage.id = :stageId " +
            "order by p.postNumber desc")
    List<Post> findByProjectIdAndStageId(@Param("projectId") Long projectId, @Param("stageId") Long stageId);

    /**
     * 프로젝트 내 최대 게시글 번호 조회
     */
    @Query("SELECT MAX(p.postNumber) FROM Post p WHERE p.project.id = :projectId")
    Optional<Long> findMaxPostNumberByProjectId(@Param("projectId") Long projectId);

    // status에 따른 post 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "join fetch Request r ON r.post.postId = p.postId " +
            "WHERE r.approveStatus = :status " +
            "ORDER BY p.createdAt DESC")
    List<Post> findPostsWithRequestStatus(@Param("status") RequestStatus status);

    /**
     * 특정 stageId의 Post와 연결된 Request 함께 조회
     */
    // stageName에 따라 조회
    @Query("SELECT p FROM Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "join fetch p.project pr " +
            "join fetch pr.company c " +
            "left join fetch p.request " +
            "WHERE p.stage.stageName = :stageName " +
            "ORDER BY p.createdAt DESC")
    List<Post> findPostsByStageName(@Param("stageName") String stageName);

    /**
     * Request가 있는 모든 Post 조회 (승인 요청이 있는 게시글만)
     */
    @Query("SELECT p FROM Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "join fetch p.project pr " +
            "join fetch pr.company c " +
            "join fetch p.request r " +
            "WHERE p.request IS NOT NULL " +
            "ORDER BY p.createdAt DESC")
    List<Post> findAllPostsWithRequest();
}
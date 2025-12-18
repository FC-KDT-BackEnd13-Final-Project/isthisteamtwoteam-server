package org.etmetmy.bn_server.domain.comment.repository;

import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 댓글 ID를 통해 프로젝트 ID를 바로 조회하는 쿼리
    @Query("SELECT p.project.id FROM Comment c " +
            "JOIN c.post p " +
            "WHERE c.commentId = :commentId")
    Long findProjectIdByCommentId(@Param("commentId") Long commentId);

    // 1. 특정 Post에 대한 최상위 댓글 조회 (parent가 NULL인 댓글, 삭제되지 않은 댓글만)
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user u " +
            "WHERE c.post.postId = :postId AND c.parent IS NULL AND c.isDeleted = false " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByPostId(@Param("postId") Long postId);

    // 2. 특정 부모 댓글 ID에 대한 대댓글 조회 (삭제되지 않은 댓글만)
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user u " +
            "WHERE c.parent = :parentCommentId AND c.isDeleted = false " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentCommentId") Long parentCommentId);

    // 3. 특정 Post의 모든 댓글 조회 (한 번에, 삭제되지 않은 댓글만)
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user u " +
            "WHERE c.post.postId = :postId AND c.isDeleted = false " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findAllByPostId(@Param("postId") Long postId);

    @Query("SELECT c.content FROM Comment c WHERE c.id = :commentId")
    String findContentById(@Param("commentId") Long commentId);
}
package org.etmetmy.bn_server.domain.comment.repository;

import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 1. 특정 Post에 대한 최상위 댓글 조회 (commentId2가 NULL인 댓글)
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user u " +
            "WHERE c.post.postId = :postId AND c.commentId2 IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByPostId(@Param("postId") Long postId);

    // 2. 특정 부모 댓글 ID에 대한 대댓글 조회
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user u " +
            "WHERE c.commentId2 = :parentCommentId " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentCommentId") Long parentCommentId);

    // 3. 특정 Post의 모든 댓글 조회 (한 번에)
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user u " +
            "WHERE c.post.postId = :postId " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findAllByPostId(@Param("postId") Long postId);
}
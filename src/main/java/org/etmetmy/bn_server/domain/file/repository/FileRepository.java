package org.etmetmy.bn_server.domain.file.repository;

import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query("SELECT f FROM File f " +
            "JOIN FETCH f.post p " +
            "WHERE p.project.id = :projectId " +
            "AND f.isDeleted = false")
    List<File> findByProjectId(@Param("projectId") Long projectId);

    // project_id로 직접 조회 (네이티브 쿼리 사용)
    @Query("SELECT f " +
            "FROM File f " +
            "WHERE f.project.id = :projectId " +
            "AND f.isDeleted = false")
    List<File> findAllByProjectId(@Param("projectId") Long projectId);

    List<File> findByPost(Post post);

    @Query("SELECT f FROM File f " +
            "WHERE f.projectCheckList.projectCheckListId = :projectCheckListId " +
            "AND f.isDeleted = false")
    List<File> findByProjectCheckListId(@Param("projectCheckListId") Long projectCheckListId);

    @Query("select f from File f " +
            "where f.post.postId = :postId and f.isDeleted = false")
    List<File> findFilesByPostId(Long postId);

    @Query("select f from File f " +
            "where f.comment.commentId = :commentId and f.isDeleted = false")
    List<File> findFilesByCommentId(Long commentId);

    @Query("select f from File f where f.comment.post.postId = :postId")
    List<File> findByPostId(@Param("postId") Long postId);
}

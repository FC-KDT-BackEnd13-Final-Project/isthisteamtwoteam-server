package org.etmetmy.bn_server.domain.file.repository;

import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    // 단일 프로젝트 ID로 삭제된 파일 조회 (업로드한 사람의 정보까지)
    @Query("SELECT f FROM File f " +
            "JOIN FETCH f.uploader " +
            "WHERE f.project.id = :projectId " +
            "AND f.isDeleted = true")
    List<File> findByDeletedFilesByProjectId(@Param("projectId") Long projectId);

    // 단일 프로젝트 ID로 삭제되지 않은 파일 조회
    @Query("SELECT f FROM File f " +
            "JOIN FETCH f.post p " +
            "WHERE p.project.id = :projectId " +
            "AND f.isDeleted = false")
    List<File> findByProjectId(@Param("projectId") Long projectId);

    // 다중 프로젝트의 게시글, 댓글, 체크리스트에 속한 파일 조회
    @Query("SELECT DISTINCT f FROM File f " +
            "WHERE f.post.project.id IN :projectIds " +
            "   OR f.comment.post.project.id IN :projectIds " +
            "   OR f.projectCheckList.project.id IN :projectIds")
    List<File> findByProjectIds(@Param("projectIds") List<Long> projectIds);

    // project_id로 업로드된 파일 조회 (isTemp=false는 post/comment/checkList 중 하나에 연결됨을 보장)
    @Query("SELECT DISTINCT f " +
            "FROM File f " +
            "LEFT JOIN FETCH f.post p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH f.comment c " +
            "LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH f.projectCheckList pcl " +
            "LEFT JOIN FETCH pcl.answererId " +
            "WHERE f.project.id = :projectId " +
            "AND f.isDeleted = false " +
            "AND f.isTemp = false")
    List<File> findAllByProjectId(@Param("projectId") Long projectId);

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

    @Query("select f from File f where f.post.postId in :postIds")
    List<File> findByPostIds(List<Long> postIds);

    /**
     * 삭제된 파일 검색 + 페이지네이션 조회 (휴지통 기능)
     */
    @Query(value = "SELECT f FROM File f " +
            "JOIN FETCH f.uploader u " +
            "WHERE f.project.id = :projectId " +
            "AND f.isDeleted = true " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "f.originalFileTitle LIKE %:keyword% OR " +
            "u.name LIKE %:keyword%) " +
            "ORDER BY f.deletedAt DESC",
            countQuery = "SELECT COUNT(f) FROM File f " +
            "JOIN f.uploader u " +
            "WHERE f.project.id = :projectId " +
            "AND f.isDeleted = true " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "f.originalFileTitle LIKE %:keyword% OR " +
            "u.name LIKE %:keyword%)")
    Page<File> findDeletedFilesByProjectIdWithSearch(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword,
            Pageable pageable);
}

package org.etmetmy.bn_server.domain.post.repository;

import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, Long> {

    // 전체 게시글 조회 (User + Stage 함께 조회)
    @Query("select p from Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "where p.project.id = :projectId " +
            "order by p.postNumber desc")
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
            "order by p.postNumber desc")
    List<Post> findUncompletedByProjectId(@Param("projectId") Long projectId);

    // 페이지네이션 지원 메서드들
    @Query(value = "select p from Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "where p.project.id = :projectId",
            countQuery = "select count(p) from Post p where p.project.id = :projectId")
    Page<Post> findAllByProjectIdWithPaging(@Param("projectId") Long projectId, Pageable pageable);

    @Query(value = "select p from Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "where p.project.id = :projectId " +
            "and p.isCompleted = true",
            countQuery = "select count(p) from Post p where p.project.id = :projectId and p.isCompleted = true")
    Page<Post> findCompletedByProjectIdWithPaging(@Param("projectId") Long projectId, Pageable pageable);

    @Query(value = "select p from Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "where p.project.id = :projectId " +
            "and p.isCompleted = false",
            countQuery = "select count(p) from Post p where p.project.id = :projectId and p.isCompleted = false")
    Page<Post> findUncompletedByProjectIdWithPaging(@Param("projectId") Long projectId, Pageable pageable);

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

    /**
     * 특정 프로젝트 목록 내에서 특정 상태의 Post 조회 (고객용 대시보드)
     */
    @Query("SELECT DISTINCT p FROM Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "join fetch Request r ON r.post.postId = p.postId " +
            "WHERE r.approveStatus = :status " +
            "AND p.project.id IN :projectIds " +
            "ORDER BY p.createdAt DESC")
    List<Post> findPostsWithRequestStatusByProjectIds(@Param("status") RequestStatus status, @Param("projectIds") List<Long> projectIds);

    @Query("SELECT p.title FROM Post p WHERE p.id = :postId")
    String findTitleById(@Param("postId") Long postId);

    /**
     * 특정 프로젝트 목록 내에서 Request가 있는 모든 Post 조회 (개발자, 고색사용 대시보드)
     */
    @Query("SELECT p FROM Post p " +
            "join fetch p.user u " +
            "join fetch p.stage s " +
            "join fetch p.project pr " +
            "join fetch pr.company c " +
            "join fetch p.request r " +
            "WHERE p.request IS NOT NULL " +
            "AND p.project.id IN :projectIds " +
            "ORDER BY p.createdAt DESC")
    List<Post> findAllPostsWithRequestByProjectIds(@Param("projectIds") List<Long> projectIds);

    /**
     * 특정 프로젝트의 삭제된 게시글 조회 (휴지통 기능)
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "JOIN FETCH p.stage " +
            "WHERE p.project.id = :projectId " +
            "AND p.isDeleted = true " +
            "ORDER BY p.deletedAt DESC")
    List<Post> findDeletedPostsByProjectId(@Param("projectId") Long projectId);

    /**
     * 삭제된 게시글 검색 + 페이지네이션 조회 (휴지통 기능)
     */
    @Query(value = "SELECT p FROM Post p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.stage s " +
            "WHERE p.project.id = :projectId " +
            "AND p.isDeleted = true " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "p.title LIKE %:keyword% OR " +
            "u.name LIKE %:keyword%) " +
            "ORDER BY p.deletedAt DESC",
            countQuery = "SELECT COUNT(p) FROM Post p " +
            "JOIN p.user u " +
            "WHERE p.project.id = :projectId " +
            "AND p.isDeleted = true " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "p.title LIKE %:keyword% OR " +
            "u.name LIKE %:keyword%)")
    Page<Post> findDeletedPostsByProjectIdWithSearch(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
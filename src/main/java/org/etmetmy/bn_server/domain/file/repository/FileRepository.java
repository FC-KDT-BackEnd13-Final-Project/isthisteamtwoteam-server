package org.etmetmy.bn_server.domain.file.repository;

import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

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

    List<File> findByPost(Post post);

    @Query("SELECT f FROM File f " +
           "WHERE f.projectCheckList.projectCheckListId = :projectCheckListId " +
           "AND f.isDeleted = false")
    List<File> findByProjectCheckListId(@Param("projectCheckListId") Long projectCheckListId);
}
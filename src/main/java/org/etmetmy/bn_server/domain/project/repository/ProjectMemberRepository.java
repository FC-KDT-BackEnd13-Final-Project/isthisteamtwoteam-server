package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    //사용자가 특정 프로젝트의 멤버인지 확인
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
           "FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId " +
           "AND pm.user.id = :userId")
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId,
                                       @Param("userId") Long userId);

    List<ProjectMember> findByProjectId(Long projectId);
    List<ProjectMember> findByProjectIdIn(List<Long> projectIds);
    ProjectMember findByUserIdAndProjectId(Long userId, Long projectId);

    // 프로젝트 + 유저 기준으로 매핑 삭제
    long deleteByProjectIdAndUserId(Long projectId, Long userId);
    
    // 권한이 있는 모든 프로젝트 조회
    @Query("""
    select pm.project.id
    from ProjectMember pm
    where pm.user.id = :userId
    """)
    List<Long> findProjectIdsByUserId(@Param("userId") Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long loginUserId);
}

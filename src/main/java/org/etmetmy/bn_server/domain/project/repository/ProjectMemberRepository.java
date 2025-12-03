package org.etmetmy.bn_server.domain.project.repository;

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

    List<ProjectMember> findByProject_Id(Long projectId);
    List<ProjectMember> findByProject_IdIn(List<Long> projectIds);
    ProjectMember findByUserIdAndProjectId(Long userId, Long projectId);
}

package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.user.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectMemberId(Long projectId);

    @Query("SELECT pm FROM ProjectMember pm JOIN pm.project p " +
            "WHERE p.projectId = :projectId")
    List<ProjectMember> findByProjectId(@Param("projectId") Long projectId);
}

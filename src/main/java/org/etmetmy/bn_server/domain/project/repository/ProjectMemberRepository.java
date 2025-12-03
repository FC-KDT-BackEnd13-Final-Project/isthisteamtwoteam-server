package org.etmetmy.bn_server.domain.project.repository;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProject_Id(Long projectId);
    List<ProjectMember> findByProject_IdIn(List<Long> projectIds);
    ProjectMember findByUserIdAndProjectId(Long userId, Long projectId);
}

package org.etmetmy.bn_server.domain.project.repository;


import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    ProjectMember findByUserIdAndProjectId(Long userId, Long projectId);

}

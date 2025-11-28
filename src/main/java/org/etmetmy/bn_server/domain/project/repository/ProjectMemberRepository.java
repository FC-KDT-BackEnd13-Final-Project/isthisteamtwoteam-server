package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.user.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
}

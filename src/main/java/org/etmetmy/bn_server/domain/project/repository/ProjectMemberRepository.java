package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.user.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProject_Id(Long projectId);
    List<ProjectMember> findByProject_IdIn(List<Long> projectIds);

    // 프로젝트 + 유저 기준으로 매핑 삭제
    long deleteByProject_IdAndUser_Id(Long projectId, Long userId);
}

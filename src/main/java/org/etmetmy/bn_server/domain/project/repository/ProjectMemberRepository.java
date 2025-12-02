package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * 사용자가 특정 프로젝트의 멤버인지 확인
     *
     * @param projectId 프로젝트 ID
     * @param userId 사용자 ID
     * @return 멤버 여부
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
           "FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId " +
           "AND pm.user.id = :userId")
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId,
                                       @Param("userId") Long userId);
}
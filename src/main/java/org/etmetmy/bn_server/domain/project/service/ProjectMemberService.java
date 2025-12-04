package org.etmetmy.bn_server.domain.project.service;

public interface ProjectMemberService {
    boolean hasRoleToProject(Long userId, Long projectId);
}

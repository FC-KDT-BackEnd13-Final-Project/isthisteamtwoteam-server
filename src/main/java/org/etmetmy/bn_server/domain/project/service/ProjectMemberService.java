package org.etmetmy.bn_server.domain.project.service;

import org.etmetmy.bn_server.domain.user.entity.ProjectMember;

import java.util.Optional;

public interface ProjectMemberService {

    boolean hasRoleToProject(Long userId, Long projectId);
}

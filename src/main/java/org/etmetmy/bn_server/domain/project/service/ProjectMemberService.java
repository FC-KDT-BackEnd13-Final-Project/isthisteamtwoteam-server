package org.etmetmy.bn_server.domain.project.service;

import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberSearchResponse;
import org.etmetmy.bn_server.domain.user.entity.Role;

import java.util.List;

public interface ProjectMemberService {
    boolean hasRoleToProject(Long userId, Long projectId);

    List<ProjectMemberSearchResponse> searchUsersForCreate(Role role);
    List<ProjectMemberSearchResponse> searchUsersForProject(Long projectId, Role role);

}

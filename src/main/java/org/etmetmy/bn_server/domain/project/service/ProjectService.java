package org.etmetmy.bn_server.domain.project.service;

import org.etmetmy.bn_server.domain.project.dto.entityDto.ProjectDTO;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {

    Long createProject(ProjectCreateRequest request, Long createdById);

    List<ProjectResponse> getAllProjects();
    List<ProjectMemberResponse> getProjectMembers(Long projectId);
}

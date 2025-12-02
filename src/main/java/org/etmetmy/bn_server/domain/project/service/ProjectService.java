package org.etmetmy.bn_server.domain.project.service;

import org.etmetmy.bn_server.domain.project.dto.request.ProjectAddCheckListRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectAddCheckListResponse;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectMemberRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectAddCheckListResponse> checklistAdd(Long projectId, ProjectAddCheckListRequest request);

    Long createProject(ProjectCreateRequest request, Long createdById);

    int addProjectMembers(Long projectId, List<ProjectMemberRequest> members, Long createdById);

    List<ProjectResponse> getAllProjects();
    List<ProjectMemberResponse> getProjectMembers(Long projectId);

    ProjectResponse getProjectById(Long projectId);

}

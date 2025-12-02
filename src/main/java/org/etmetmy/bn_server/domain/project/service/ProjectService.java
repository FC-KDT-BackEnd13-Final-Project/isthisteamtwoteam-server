package org.etmetmy.bn_server.domain.project.service;

import org.etmetmy.bn_server.domain.project.dto.request.*;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectAddCheckListResponse;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectMemberRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {


    Long createProject(ProjectCreateRequest request, Long createdById);

    int addProjectMembers(Long projectId, List<ProjectMemberRequest> members, Long createdById);

    List<ProjectResponse> getAllProjects();
    List<ProjectMemberResponse> getProjectMembers(Long projectId);

    ProjectResponse getProjectById(Long projectId);
    List<ProjectAddCheckListResponse> checklistAdd(Long projectId, ProjectAddCheckListRequest request);

    // 프로젝트 제목 수정
    void updateProjectTitle(Long projectId, ProjectTitleUpdateRequest request);

    // 프로젝트 날짜 수정
    void updateProjectDate(Long projectId, ProjectDateUpdateRequest request);

}

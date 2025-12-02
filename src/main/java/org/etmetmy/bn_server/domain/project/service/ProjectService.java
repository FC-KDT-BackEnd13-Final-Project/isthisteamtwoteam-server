package org.etmetmy.bn_server.domain.project.service;

import org.etmetmy.bn_server.domain.project.dto.request.ProjectAddCheckListRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectAddCheckListResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectAddCheckListResponse> checklistAdd(Long projectId, ProjectAddCheckListRequest request);
}

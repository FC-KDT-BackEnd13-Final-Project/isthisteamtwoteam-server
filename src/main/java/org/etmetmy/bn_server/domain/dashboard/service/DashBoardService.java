package org.etmetmy.bn_server.domain.dashboard.service;

import org.etmetmy.bn_server.domain.dashboard.dto.response.ProjectListResponse;

import java.util.List;

public interface DashBoardService {

    // 프로젝트 목록 조회
    List<ProjectListResponse> getProjectList(Long loginUserId);
}

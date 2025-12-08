package org.etmetmy.bn_server.domain.project.service;

import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberSearchResponse;

import java.util.List;

public interface ProjectMemberService {
    boolean hasRoleToProject(Long userId, Long projectId);

    // 프로젝트 생성 - 개발사/고객사 담당자, 사원조회
    List<ProjectMemberSearchResponse> searchDeveloperMembers();
    List<ProjectMemberSearchResponse> searchClientMembers();

    // 프로젝트 설정 - 개발사/고객사 담당자, 사원 조회

}

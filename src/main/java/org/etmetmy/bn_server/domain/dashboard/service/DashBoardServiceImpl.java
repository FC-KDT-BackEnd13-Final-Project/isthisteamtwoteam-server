package org.etmetmy.bn_server.domain.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ProjectListResponse;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashBoardService{

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // 1. 프로젝트 목록 조회 (접근 권한 정보 제공)
    public List<ProjectListResponse> getProjectList(Long loginUserId){

        // 유저 검증
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);

        // 모든 프로젝트 목록 조회
        List<Project> allProjects = projectRepository.findAll();

        // 권한이 있는 모든 프로젝트 조회
        List<Long> myProjectIds = projectMemberRepository.findProjectIdsByUserId(loginUserId);

        return ProjectListResponse.Converter.from(allProjects, myProjectIds);
    }
}

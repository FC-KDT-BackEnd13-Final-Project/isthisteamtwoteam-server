package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.project.dto.entityDto.ProjectDTO;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectMemberRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.ProjectMember;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Long createProject(ProjectCreateRequest request, Long createdById){

        Project project = ProjectCreateRequest.Converter.toEntity(request, createdById);
        Project savedProject = projectRepository.save(project);

        // 2. 프로젝트 멤버 생성 및 저장
        if (hasMembers(request)) {
            List<ProjectMember> projectMembers = createProjectMembers(
                    request.getMembers(),
                    savedProject,
                    createdById
            );
            projectMemberRepository.saveAll(projectMembers);

            log.info("프로젝트 생성 완료 - ID: {}, 멤버 수: {}",
                    savedProject.getProjectId(),
                    projectMembers.size()
            );
        } else {
            log.info("프로젝트 생성 완료 - ID: {} (멤버 없음)", savedProject.getProjectId());
        }

        return savedProject.getProjectId();
    }

    private boolean hasMembers(ProjectCreateRequest request) {
        return request.getMembers() != null && !request.getMembers().isEmpty();
    }

    private List<ProjectMember> createProjectMembers(
            List<ProjectMemberRequest> members,
            Project project,
            Long createdById
    ) {
        // 1. 중복 제거된 userId 리스트 추출
        List<Long> memberIds = members.stream()
                .map(ProjectMemberRequest::getUserId)
                .distinct()
                .toList();

        // 2. userId로 User 엔티티 일괄 조회
        Map<Long, User> userMap = userRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 3. 존재하지 않는 사용자 ID 로깅
        List<Long> notFoundUserIds = memberIds.stream()
                .filter(id -> !userMap.containsKey(id))
                .toList();

        if (!notFoundUserIds.isEmpty()) {
            log.warn("존재하지 않는 사용자 ID 목록: {}", notFoundUserIds);

            // 선택사항: 예외를 던지고 싶다면 아래 주석 해제
            // throw new IllegalArgumentException(
            //     "존재하지 않는 사용자 ID: " + notFoundUserIds
            // );
        }

        // 4. 존재하는 User만 필터링하여 ProjectMember 생성
        return members.stream()
                .filter(req -> userMap.containsKey(req.getUserId()))
                .map(req -> {
                    User user = userMap.get(req.getUserId());
                    // ProjectMemberRequest.Converter 활용
                    return ProjectMemberRequest.Converter.toEntity(
                            req,
                            project,
                            user,
                            createdById
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return ProjectDTO.Converter.toResponseList(projects);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        log.info("[프로젝트 멤버 조회] projectId={}, memberCount={}", projectId, members.size());
        return ProjectMemberResponse.Converter.from(members);
    }

}
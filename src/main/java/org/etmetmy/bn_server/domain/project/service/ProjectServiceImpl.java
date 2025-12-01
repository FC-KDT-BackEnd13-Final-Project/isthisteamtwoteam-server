package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.repository.MemoRepository;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.project.dto.entity.ProjectDTO;
import org.etmetmy.bn_server.domain.project.repository.ProjectStageRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService{

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectStageRepository projectStageRepository;
    private final MemoRepository memoRepository;

    @Override
    @Transactional
    public Long createProject(ProjectCreateRequest request, Long createdById){

        Stage startStage = getStartStage(request.getStage());

        // 2. Project 생성 및 저장 (시작 단계 설정 포함)
        Project project = ProjectCreateRequest.Converter.toEntity(request, createdById, startStage);
        Project savedProject = projectRepository.save(project);

        // 3. Memo 생성 및 저장
        createMemoIfPresent(request.getMemo(), savedProject);

        // 4. 프로젝트 멤버 생성 및 저장
        if (hasMembers(request)) {
            List<ProjectMember> projectMembers = createProjectMembers(
                    request.getMembers(),
                    savedProject,
                    createdById
            );
            projectMemberRepository.saveAll(projectMembers);

            log.info("프로젝트 생성 완료 - ID: {}, 시작단계: {}, 멤버 수: {}",
                    savedProject.getId(),
                    startStage.getStageName(),
                    projectMembers.size()
            );
        } else {
            log.info("프로젝트 생성 완료 - ID: {} (시작단계: {}, 멤버 없음)",
                    savedProject.getId(),
                    startStage.getStageName()
            );
        }

        return savedProject.getId();
    }

    @Override
    @Transactional
    public int addProjectMembers(Long projectId, List<ProjectMemberRequest> members, Long createdById) {
        if (members == null || members.isEmpty()) {
            return 0;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트입니다: " + projectId));

        List<ProjectMember> projectMembers = createProjectMembers(members, project, createdById);
        projectMemberRepository.saveAll(projectMembers);
        log.info("프로젝트 멤버 수동 추가 - projectId: {}, 추가된 멤버 수: {}", projectId, projectMembers.size());
        return projectMembers.size();
    }

    private Stage getStartStage(String stageName) {
        String normalized = normalizeStageName(stageName);

        return projectStageRepository.findByStageName(normalized)
                .or(() -> projectStageRepository.findByStageNameNormalized(normalized))
                .orElseGet(() -> createStage(normalized));
    }

    private String normalizeStageName(String stageName) {
        String normalized = stageName != null ? stageName.trim() : null;

        if (normalized == null || normalized.isBlank()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "프로젝트 단계가 지정되지 않았습니다.");
        }
        return normalized;
    }

    private Stage createStage(String stageName) {
        Stage newStage = Stage.builder()
                .stageName(stageName)
                .build();
        return projectStageRepository.save(newStage);
    }

    private void createMemoIfPresent(String memoContent, Project project) {
        if (memoContent == null || memoContent.isBlank()) {
            return;
        }

        Memo memo = Memo.builder()
                .project(project)
                .content(memoContent)
                .build();
        memoRepository.save(memo);
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
        }

        // 4. 존재하는 User만 필터링하여 ProjectMember 생성
        return members.stream()
                .filter(req -> userMap.containsKey(req.getUserId()))
                .map(req -> {
                    User user = userMap.get(req.getUserId());
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
        Map<Long, List<ProjectMember>> membersByProjectId = groupMembersByProject(projects);
        return ProjectDTO.Converter.toResponseList(projects, membersByProjectId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);
        log.info("[프로젝트 멤버 조회] projectId={}, memberCount={}", projectId, members.size());
        return ProjectMemberResponse.Converter.from(members);
    }

    private Map<Long, List<ProjectMember>> groupMembersByProject(List<Project> projects) {
        List<Long> projectIds = projects.stream()
                .map(Project::getId)
                .filter(id -> id != null)
                .toList();

        if (projectIds.isEmpty()) {
            return Map.of();
        }

        return projectMemberRepository.findByProject_IdIn(projectIds).stream()
                .collect(Collectors.groupingBy(pm -> pm.getProject().getId()));
    }

}

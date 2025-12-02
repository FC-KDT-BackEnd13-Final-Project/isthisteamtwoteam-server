package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectAddCheckListRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectAddCheckListResponse;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.domain.project.repository.ProjectCheckListRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;
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

public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectCheckListRepository projectChecklistRepository;
    private final CheckListRepository checkListRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectStageRepository projectStageRepository;
    private final MemoRepository memoRepository;
    
    @Override
    public List<ProjectAddCheckListResponse> checklistAdd(Long projectId, ProjectAddCheckListRequest request) {

        // project 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(StatusCode.PROJECT_NOT_FOUND));

        // 여러 checklistId에 대해 ProjectChecklist 엔티티 생성
        List<ProjectCheckList> projectCheckLists = request.getChecklistIds().stream()
                .map(checkListId ->{
                    // 각 CheckList 조회
                    CheckList checkList = checkListRepository.findById(checkListId)
                            .orElseThrow(()-> new CustomException(StatusCode.CHECKLIST_NOT_FOUND));

                    // ProjectCheckList 엔티티 생성
                    return ProjectAddCheckListRequest.Converter.toEntity(project, checkList);
                })
                .toList();

        // 일괄 저장
        List<ProjectCheckList> savedCheckLists = projectChecklistRepository.saveAll(projectCheckLists);

        // Response 별환 후 반환
        return ProjectAddCheckListResponse.Converter.from(savedCheckLists);
    }

    @Override
    @Transactional
    public Long createProject(ProjectCreateRequest request, Long createdById){

        log.info("=== 프로젝트 생성 시작 ===");
        log.info("요청 데이터 - projectName: {}, members: {}", request.getProjectName(), request.getMembers());

        Stage startStage = getStartStage(request.getStage());

        // 2. Project 생성 및 저장 (시작 단계 설정 포함)
        Project project = ProjectCreateRequest.Converter.toEntity(request, createdById, startStage);
        Project savedProject = projectRepository.save(project);

        log.info("프로젝트 저장 완료 - projectId: {}", savedProject.getId());

        // 3. Memo 생성 및 저장
        createMemoIfPresent(request.getMemo(), savedProject);

        // 4. 프로젝트 멤버 생성 및 저장
        if (hasMembers(request)) {
            log.info("멤버 목록 존재 - 개수: {}, IDs: {}", request.getMembers().size(), request.getMembers());

            // List<Long>을 List<ProjectMemberRequest>로 변환 (생성자 사용)
            List<ProjectMemberRequest> memberRequests = request.getMembers().stream()
                    .map(userId -> {
                        ProjectMemberRequest memberRequest = new ProjectMemberRequest(userId);
                        log.debug("ProjectMemberRequest 생성 - userId: {}", userId);
                        return memberRequest;
                    })
                    .collect(Collectors.toList());

            log.info("ProjectMemberRequest 변환 완료 - 개수: {}", memberRequests.size());

            List<ProjectMember> projectMembers = createProjectMembers(
                    memberRequests,
                    savedProject,
                    createdById
            );

            log.info("ProjectMember 생성 완료 - 개수: {}", projectMembers.size());

            // 빈 리스트가 아닐 때만 저장
            if (!projectMembers.isEmpty()) {
                List<ProjectMember> saved = projectMemberRepository.saveAll(projectMembers);
                log.info("DB 저장 완료 - 저장된 멤버 수: {}", saved.size());

                // 저장 확인
                saved.forEach(pm ->
                        log.info("저장된 ProjectMember - ID: {}, ProjectID: {}, UserID: {}",
                                pm.getProjectMemberId(),
                                pm.getProject().getId(),
                                pm.getUser() != null ? pm.getUser().getId() : "null")
                );

                log.info("프로젝트 생성 완료 - ID: {}, 시작단계: {}, 멤버 수: {}",
                        savedProject.getId(),
                        startStage.getStageName(),
                        saved.size()
                );
            } else {
                log.warn("프로젝트 생성 완료 - ID: {} (시작단계: {}, 유효한 멤버 없음)",
                        savedProject.getId(),
                        startStage.getStageName()
                );
            }
        } else {
            log.warn("멤버 목록이 비어있음 - members: {}", request.getMembers());
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

        if (projectMembers.isEmpty()) {
            log.warn("프로젝트 멤버 추가 실패 - projectId: {}, 유효한 사용자 없음", projectId);
            return 0;
        }

        projectMemberRepository.saveAll(projectMembers);
        log.info("프로젝트 멤버 수동 추가 - projectId: {}, 추가된 멤버 수: {}", projectId, projectMembers.size());
        return projectMembers.size();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();

        log.info("전체 프로젝트 조회 - 개수: {}", projects.size());

        Map<Long, List<ProjectMember>> membersByProjectId = groupMembersByProject(projects);

        log.info("프로젝트별 멤버 맵 - 크기: {}", membersByProjectId.size());
        membersByProjectId.forEach((projectId, members) ->
                log.info("ProjectID: {} - 멤버 수: {}", projectId, members.size())
        );

        return ProjectResponse.Converter.from(projects, membersByProjectId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        log.info("=== 프로젝트 멤버 조회 시작 - projectId: {} ===", projectId);

        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);

        log.info("조회된 멤버 수: {}", members.size());
        members.forEach(pm ->
                log.info("멤버 상세 - ProjectMemberID: {}, UserID: {}, UserName: {}",
                        pm.getProjectMemberId(),
                        pm.getUser() != null ? pm.getUser().getId() : "null",
                        pm.getUser() != null ? pm.getUser().getName() : "null")
        );

        return ProjectMemberResponse.Converter.from(members);
    }

    @Transactional(readOnly = true)
    @Override
    public ProjectResponse getProjectById(Long projectId) {
        log.info("=== 단일 프로젝트 조회 - projectId: {} ===", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트입니다: " + projectId));

        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);

        log.info("프로젝트 ID: {}, 멤버 수: {}", projectId, members.size());

        return ProjectResponse.Converter.from(project, members);
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로젝트 단계가 지정되지 않았습니다.");
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
        boolean result = request.getMembers() != null && !request.getMembers().isEmpty();
        log.debug("hasMembers 체크 - members: {}, result: {}", request.getMembers(), result);
        return result;
    }

    private List<ProjectMember> createProjectMembers(
            List<ProjectMemberRequest> members,
            Project project,
            Long createdById
    ) {
        log.info("=== createProjectMembers 시작 ===");
        log.info("입력 멤버 요청 수: {}", members.size());

        // 1. 중복 제거된 userId 리스트 추출
        List<Long> memberIds = members.stream()
                .map(ProjectMemberRequest::getUserId)
                .distinct()
                .toList();

        log.info("중복 제거 후 userId 목록: {}", memberIds);

        // 2. userId로 User 엔티티 일괄 조회
        List<User> users = userRepository.findAllById(memberIds);
        log.info("DB에서 조회된 User 수: {}", users.size());
        users.forEach(user -> log.info("조회된 User - ID: {}, Name: {}", user.getId(), user.getName()));

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 3. 존재하지 않는 사용자 ID 체크 및 예외 발생
        List<Long> notFoundUserIds = memberIds.stream()
                .filter(id -> !userMap.containsKey(id))
                .toList();

        if (!notFoundUserIds.isEmpty()) {
        }

        // 4. 존재하는 User만 필터링하여 ProjectMember 생성
        List<ProjectMember> projectMembers = members.stream()
                .filter(req -> {
                    boolean exists = userMap.containsKey(req.getUserId());
                    log.debug("userId: {} 존재 여부: {}", req.getUserId(), exists);
                    return exists;
                })
                .map(req -> {
                    User user = userMap.get(req.getUserId());
                    ProjectMember pm = ProjectMemberRequest.Converter.toEntity(
                            req,
                            project,
                            user,
                            createdById
                    );
                    log.info("ProjectMember 생성 - UserID: {}, ProjectID: {}", user.getId(), project.getId());
                    return pm;
                })
                .toList();

        log.info("프로젝트 멤버 생성 완료 - 전체: {}, 유효: {}, 무효: {}",
                memberIds.size(), projectMembers.size(), notFoundUserIds.size());

        return projectMembers;
    }

    private Map<Long, List<ProjectMember>> groupMembersByProject(List<Project> projects) {
        List<Long> projectIds = projects.stream()
                .map(Project::getId)
                .filter(id -> id != null)
                .toList();

        if (projectIds.isEmpty()) {
            return Map.of();
        }

        List<ProjectMember> allMembers = projectMemberRepository.findByProject_IdIn(projectIds);
        log.info("전체 프로젝트의 멤버 조회 - 총 멤버 수: {}", allMembers.size());

        return allMembers.stream()
                .collect(Collectors.groupingBy(pm -> pm.getProject().getId()));
    }
}
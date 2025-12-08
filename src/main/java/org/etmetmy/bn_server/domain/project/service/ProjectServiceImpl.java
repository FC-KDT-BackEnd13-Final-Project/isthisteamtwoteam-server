package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.repository.MemoRepository;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.file.dto.FileInfoDTO;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;
import org.etmetmy.bn_server.domain.project.dto.request.*;
import org.etmetmy.bn_server.domain.project.dto.response.*;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.etmetmy.bn_server.domain.project.repository.ProjectStageRepository;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.domain.project.repository.ProjectCheckListRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.custom.InvalidInputException;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
    private final ProjectCheckListRepository projectChecklistRepository;
    private final CheckListRepository checkListRepository;
    private final FileRepository fileRepository;
    private final LinkRepository linkRepository;

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
            // List<Long>을 List<ProjectMemberRequest>로 변환 (생성자 사용)
            List<ProjectMemberRequest> memberRequests = request.getMembers().stream()
                    .map(ProjectMemberRequest::new)
                    .collect(Collectors.toList());

            List<ProjectMember> projectMembers = createProjectMembers(
                    memberRequests,
                    savedProject,
                    createdById
            );
            // 빈 리스트가 아닐 때만 저장
            if (!projectMembers.isEmpty()) {
                projectMemberRepository.saveAll(projectMembers);
            }
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
                .orElseThrow(ProjectNotFoundException::new);

        List<ProjectMember> projectMembers = createProjectMembers(members, project, createdById);

        if (projectMembers.isEmpty()) {
            return 0;
        }

        projectMemberRepository.saveAll(projectMembers);
        return projectMembers.size();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();

        Map<Long, List<ProjectMember>> membersByProjectId = groupMembersByProject(projects);

        return ProjectResponse.Converter.from(projects, membersByProjectId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);

        return ProjectMemberResponse.Converter.from(members);
    }

    @Transactional(readOnly = true)
    @Override
    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);

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
            throw new InvalidInputException("프로젝트 단계가 지정되지 않았습니다.");
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
        List<User> users = userRepository.findAllById(memberIds);

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 4. 존재하는 User만 필터링하여 ProjectMember 생성
        List<ProjectMember> projectMembers = members.stream()
                .filter(req -> userMap.containsKey(req.getUserId()))
                .map(req -> {
                    User user = userMap.get(req.getUserId());
                    ProjectMember pm = ProjectMemberRequest.Converter.toEntity(
                            req,
                            project,
                            user,
                            createdById
                    );
                    return pm;
                })
                .toList();

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

        return allMembers.stream()
                .collect(Collectors.groupingBy(pm -> pm.getProject().getId()));
    }

    @Override
    public List<ProjectAddCheckListResponse> checklistAdd(Long projectId, ProjectAddCheckListRequest request) {

        // project 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // 여러 checklistId에 대해 ProjectChecklist 엔티티 생성
        List<ProjectCheckList> projectCheckLists = request.getChecklistIds().stream()
                .map(checkListId ->{
                    // 각 CheckList 조회
                    CheckList checkList = checkListRepository.findById(checkListId)
                            .orElseThrow(()-> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

                    // ProjectCheckList 엔티티 생성
                    return ProjectAddCheckListRequest.Converter.toEntity(project, checkList);
                })
                .toList();

        // 일괄 저장
        List<ProjectCheckList> savedCheckLists = projectChecklistRepository.saveAll(projectCheckLists);

        // Response 변환 후 반환
        return ProjectAddCheckListResponse.Converter.from(savedCheckLists);
    }

    @Override
    public List<ProjectCheckListAllResponse> getCheckLists(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        List<ProjectCheckList> projectCheckLists = projectChecklistRepository.findByProject(project);

        // 각 ProjectCheckList에 대해 File과 Link를 조회하여 Response 생성
        return projectCheckLists.stream()
                .map(projectCheckList -> {
                    Long checkListId = projectCheckList.getProjectCheckListId();

                    // File 조회 및 DTO 변환 (ID 기반 조회로 변경)
                    List<File> files = fileRepository.findByProjectCheckListId(checkListId);
                    List<FileInfoDTO> fileDTOs = FileInfoDTO.Converter.from(files);

                    // Link 조회 및 DTO 변환 (ID 기반 조회로 변경)
                    List<Link> links = linkRepository.findByProjectCheckListId(checkListId);
                    List<LinkInfoDTO> linkDTOs = LinkInfoDTO.Converter.from(links);

                    // Response 생성
                    return ProjectCheckListAllResponse.Converter.from(projectCheckList, fileDTOs, linkDTOs);
                })
                .toList();
    }

    //프로젝트 제목수정
    @Transactional
    @Override
    public ProjectUpdateResponse updateProjectName(Long projectId, ProjectNameUpdateRequest request) {
        if (projectId == null) {
            throw new InvalidInputException("프로젝트 ID가 필요합니다.");
        }

        String newProjectName = request.getProjectName();
        if (newProjectName == null || newProjectName.isBlank()) {
            throw new InvalidInputException("프로젝트 제목은 비워둘 수 없습니다.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        int updated = projectRepository.updateProjectName(project.getId(), newProjectName);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "프로젝트 제목 수정에 실패했습니다.");
        }

        Project updatedProject = projectRepository.findById(projectId)
                .orElseThrow((ProjectNotFoundException::new));

        return ProjectUpdateResponse.Converter.from(updatedProject);
    }

    //프로젝트 날짜 수정
    @Transactional
    @Override
    public ProjectUpdateResponse updateProjectDate(Long projectId, ProjectDateUpdateRequest request) {
        if (projectId == null) {
            throw new InvalidInputException("프로젝트 ID가 필요합니다.");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new InvalidInputException("시작일과 종료일은 모두 필수입니다.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        try {
            LocalDate startDate = parseDate(request.getStartDate());
            LocalDate endDate = parseDate(request.getEndDate());

            if (endDate.isBefore(startDate)) {
                throw new InvalidInputException("종료일은 시작일보다 이를 수 없습니다.");
            }

            int updated = projectRepository.updateProjectDates(project.getId(), startDate, endDate);
            if (updated == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "프로젝트 날짜 수정에 실패했습니다.");
            }

            return ProjectUpdateResponse.Converter.from(project);

        } catch (DateTimeParseException e) {
            throw new InvalidInputException("날짜 형식이 올바르지 않습니다. (예: 2024-01-01 또는 2025-11-23T14:00:00Z)");
        }
    }

    //날짜 형식 변경
    private  LocalDate parseDate(String dateStr){
        if (dateStr.contains("T")) {
            return LocalDate.parse(dateStr.substring(0,10));
        } else {
            return LocalDate.parse(dateStr);
        }
    }

    //프로젝트 삭제(휴지통이동)
    @Transactional
    @Override
    public ProjectTrashResponse deleteProject(Long projectId) {
        if (projectId == null) {
            throw new InvalidInputException("프로젝트 ID가 필요합니다.");
        }
        // 프로젝트 존재 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
        // 이미 삭제된 프로젝트인지 확인
        if (project.getIsDeleted() != null && project.getIsDeleted()) {
            throw new BusinessException(ErrorCode.PROJECT_CANNOT_DELETE, "이미 삭제된 프로젝트입니다.");
        }
        // 휴지통으로 이동 (소프트 삭제)
        LocalDateTime deletedAt = LocalDateTime.now();
        int updated = projectRepository.moveToTrash(projectId, deletedAt);

        if (updated == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "프로젝트 삭제에 실패했습니다.");
        }

        return ProjectTrashResponse.Converter.from(projectId);
    }


    @Transactional
    @Override
    public void removeProjectMember(Long projectId, Long userId) {
        // 1. 프로젝트 존재 + 삭제 여부 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        if (Boolean.TRUE.equals(project.getIsDeleted())) {
            throw new BusinessException(ErrorCode.PROJECT_CANNOT_DELETE, "삭제된 프로젝트의 멤버는 수정할 수 없습니다.");
        }

        // 2. 사용자 존재 확인 (선택 - 필요 없으면 이 부분은 빼도 됨)
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 3. 프로젝트-멤버 매핑 삭제
        long deletedCount = projectMemberRepository.deleteByProject_IdAndUser_Id(projectId, userId);

        if (deletedCount == 0) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "프로젝트에 해당 멤버가 존재하지 않습니다.");
        }
    }

    //프로젝트 진행단계 수정
    @Transactional
    @Override
    public ProjectStageUpdateResponse updateProjectStage(Long projectId,
                                                         ProjectStageUpdateRequest request,
                                                         Long currentUserId) {
        // 1. 입력값 검증
        if (projectId == null) {
            throw new InvalidInputException("프로젝트 ID가 필요합니다.");
        }
        if (request == null || request.getStageId() == null) {
            throw new InvalidInputException("진행단계 ID는 필수입니다.");
        }

        Long stageId = request.getStageId();

        // 2. 프로젝트 존재 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // 3. Stage 존재 확인
        Stage stage = projectStageRepository.findById(stageId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.STAGE_NOT_FOUND, "해당 진행단계를 찾을 수 없습니다."));

        // 4. 현재 사용자 조회 (존재 시 updatedBy 노출)
        Long updatedBy = null;
        if (currentUserId != null) {
            userRepository.findById(currentUserId)
                    .orElseThrow(UserNotFoundException::new);
            updatedBy = currentUserId;
        }

        // 5. Project 의 stage FK 업데이트
        int updated = projectRepository.updateProjectStage(projectId, stage.getId());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "프로젝트 진행단계 수정에 실패했습니다.");
        }

        // 6. 응답 반환 (ADMIN 이면 ID, 아니면 null)
        return ProjectStageUpdateResponse.Converter.of(stageId, updatedBy);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        return ProjectDetailResponse.Converter.from(project);
    }
}

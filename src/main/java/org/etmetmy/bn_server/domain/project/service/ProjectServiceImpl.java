package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.file.service.FileServiceImpl;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.repository.MemoRepository;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;
import org.etmetmy.bn_server.domain.project.dto.request.*;
import org.etmetmy.bn_server.domain.project.dto.response.*;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectTrashResponse;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.etmetmy.bn_server.domain.project.repository.ProjectStageRepository;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.domain.project.repository.ProjectCheckListRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.custom.InvalidInputException;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectStageRepository projectStageRepository;
    private final MemoRepository memoRepository;
    private final ProjectCheckListRepository projectChecklistRepository;
    private final CheckListRepository checkListRepository;
    private final FileRepository fileRepository;
    private final LinkRepository linkRepository;
    private final CompanyRepository companyRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public void createProject(ProjectCreateRequest request, MultipartFile image, Long loginUserId) {

        // 1. 엔티티 조회
        Stage startStage = getStartStage(request.getStage());
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. Project 생성 및 저장 (시작 단계 설정 포함)
        Project project = ProjectCreateRequest.Converter.toEntity(request, company, loginUserId, startStage);
        Project savedProject = projectRepository.save(project);

        // 3. 이미지가 있을 경우 S3에 업로드하고 URL 받기
        if (image != null && !image.isEmpty()) {
            // S3 업로드
            String imageUrl = fileService.uploadToS3(image);
            // 프로젝트에 이미지 주소 저장
            project.setProjectImageUrl(imageUrl);
        }

        // 4. Memo 생성 및 저장
        createMemoIfPresent(request.getMemo(), savedProject);

        // 5. 프로젝트 멤버 생성 및 저장
        if (hasMembers(request)) {
            // List<Long>을 List<ProjectMemberRequest>로 변환 (생성자 사용)
            List<ProjectMemberRequest> memberRequests = request.getMembers().stream()
                    .map(ProjectMemberRequest::new)
                    .collect(Collectors.toList());

            List<ProjectMember> projectMembers = createProjectMembers(
                    memberRequests,
                    savedProject,
                    loginUserId
            );
            // 빈 리스트가 아닐 때만 저장
            if (!projectMembers.isEmpty()) {
                projectMemberRepository.saveAll(projectMembers);
            }
        }

        // 6. 체크리스트 생성 및 저장
        if (request.getSelectedChecklistIds() != null && !request.getSelectedChecklistIds().isEmpty()) {
            List<ProjectCheckList> projectCheckLists = request.getSelectedChecklistIds().stream()
                    .map(checkListId -> {
                        Long checkListIdLong = checkListId.longValue();
                        // CheckList 존재 여부 확인
                        checkListRepository.findById(checkListIdLong)
                                .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

                        // ProjectAddCheckListRequest.Converter 재사용
                        return ProjectAddCheckListRequest.Converter.toEntity(savedProject, checkListIdLong);
                    })
                    .toList();

            if (!projectCheckLists.isEmpty()) {
                projectChecklistRepository.saveAll(projectCheckLists);
            }
        }
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
        List<Project> projects = projectRepository.findActiveProjects();

        Map<Long, List<ProjectMember>> membersByProjectId = groupMembersByProject(projects);

        return ProjectResponse.Converter.from(projects, membersByProjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectCustomerResponse> getAllProjects(Long loginUserId) {
        // 1. loginUserId로 User 조회 및 검증
        User user = userRepository.findById(loginUserId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 해당 user가 속한 프로젝트 ID들을 조회
        List<Long> projectIds = projectMemberRepository.findProjectIdsByUserId(user.getId());

        // 3. 프로젝트 ID가 없으면 빈 리스트 반환
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        // 4. 프로젝트 조회
        List<Project> projects = projectRepository.findAllById(projectIds);

        // 5. ProjectCustomerResponse로 변환하여 반환
        return ProjectCustomerResponse.Converter.from(projects);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);

        return ProjectMemberResponse.Converter.from(members);
    }

    @Transactional(readOnly = true)
    @Override
    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);

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

        List<ProjectMember> allMembers = projectMemberRepository.findByProjectIdIn(projectIds);

        return allMembers.stream()
                .collect(Collectors.groupingBy(pm -> pm.getProject().getId()));
    }

    @Override
    public List<ProjectAddCheckListResponse> checklistAdd(Long projectId, ProjectAddCheckListRequest request) {

        // project 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // CheckList 조회 및 Map 생성
        Map<Long, CheckList> checkListMap = request.getChecklistIds().stream()
                .map(checkListId -> checkListRepository.findById(checkListId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND)))
                .collect(Collectors.toMap(CheckList::getCheckListId, checkList -> checkList));

        // 여러 checklistId에 대해 ProjectChecklist 엔티티 생성
        List<ProjectCheckList> projectCheckLists = request.getChecklistIds().stream()
                .map(checkListId -> ProjectAddCheckListRequest.Converter.toEntity(project, checkListId))
                .toList();

        // 일괄 저장
        List<ProjectCheckList> savedCheckLists = projectChecklistRepository.saveAll(projectCheckLists);

        // Response 변환 후 반환
        return ProjectAddCheckListResponse.Converter.from(savedCheckLists, checkListMap);
    }

    @Override
    public List<ProjectCheckListAllResponse> getCheckLists(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        List<ProjectCheckList> projectCheckLists = projectChecklistRepository.findByProject(project);

        // 각 ProjectCheckList에 대해 CheckList, File, Link를 조회하여 Response 생성
        return projectCheckLists.stream()
                .map(projectCheckList -> {
                    Long projectCheckListId = projectCheckList.getProjectCheckListId();
                    Long checkListId = projectCheckList.getCheckListId();

                    // CheckList 조회
                    CheckList checkList = checkListRepository.findById(checkListId)
                            .orElseThrow(()-> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

                    // File 조회 및 DTO 변환
                    List<File> files = fileRepository.findByProjectCheckListId(projectCheckListId);
                    List<FileInfoDTO> fileDTOs = FileInfoDTO.Converter.from(files);

                    // Link 조회 및 DTO 변환
                    List<Link> links = linkRepository.findByProjectCheckListId(projectCheckListId);
                    List<LinkInfoDTO> linkDTOs = LinkInfoDTO.Converter.from(links);

                    // Response 생성
                    return ProjectCheckListAllResponse.Converter.from(projectCheckList, checkList, fileDTOs, linkDTOs);
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
    private LocalDate parseDate(String dateStr) {
        if (dateStr.contains("T")) {
            return LocalDate.parse(dateStr.substring(0, 10));
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
        long deletedCount = projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);

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

    // (휴지통 페이지) 삭제된 프로젝트 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<DeletedProjectResponse> getDeletedProjectList(Long loginUserId) {
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);

        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.DELETED_PROJECT_ACCESS_DENIED);
        }
        List<Project> projects = projectRepository.findDeletedProjects();

        return DeletedProjectResponse.Converter.from(projects);
    }

    // 삭제된 프로젝트 복원
    @Override
    @Transactional
    public ProjectRestoreResponse restoreDeletedProject(Long loginUserId, ProjectRestoreRequest request) {

        // 권한 검증
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);
        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.DELETED_PROJECT_ACCESS_DENIED);
        }

        // 요청한 프로젝트 ID 조회
        List<Long> projectIds = request.getProjectIds();
        List<Project> projects = projectRepository.findAllById(projectIds);

        // 1. 존재 개수 비교
        if (projects.size() != projectIds.size()) {
            throw new ProjectNotFoundException("존재하지 않는 프로젝트가 포함되어 있습니다.");
        }

        // 2. 삭제 여부 체크
        projects.forEach(project -> {
            if (!project.getIsDeleted()) {
                throw new BusinessException(ErrorCode.PROJECT_NOT_DELETED);
            }
            project.restore();
        });

        projectRepository.saveAll(projects);
        return ProjectRestoreResponse.Converter.from(projects);
    }

    // 프로젝트 이미지 수정
    @Override
    @Transactional
    public ProjectUpdateResponse updateProjectImage(Long projectId, MultipartFile image){

        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // 이미지가 있을 경우 S3에 업로드하고 URL 받기
        if (image != null && !image.isEmpty()) {
            // S3 업로드
            String imageUrl = fileService.uploadToS3(image);
            // 프로젝트에 이미지 주소 저장
            project.setProjectImageUrl(imageUrl);
            projectRepository.save(project);
        }
        return ProjectUpdateResponse.Converter.from(project);
    }
}

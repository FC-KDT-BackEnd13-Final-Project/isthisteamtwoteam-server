package org.etmetmy.bn_server.domain.project.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.entity.MemoType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ProjectCreateResponse createProject(ProjectCreateRequest request, Long loginUserId) {

        // 1. Stage 조회
        Stage startStage = getStartStage(request.getStage());

        // 2. Company 조회
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 3. Project 생성
        Project project = ProjectCreateRequest.Converter
                .toEntity(request, company, loginUserId, startStage);

        Project savedProject = projectRepository.save(project);

        // 4. Memo
        createMemoIfPresent(request.getMemo(), savedProject);

        // 5. Project Members
        if (hasMembers(request)) {
            List<ProjectMemberRequest> memberRequests = request.getMembers().stream()
                    .map(ProjectMemberRequest::new)
                    .toList();

            List<ProjectMember> members = createProjectMembers(
                    memberRequests,
                    savedProject,
                    loginUserId
            );

            if (!members.isEmpty()) {
                projectMemberRepository.saveAll(members);
            }
        }

        // 6. CheckLists
        if (request.getSelectedChecklistIds() != null && !request.getSelectedChecklistIds().isEmpty()) {
            List<ProjectCheckList> projectCheckLists =
                    request.getSelectedChecklistIds().stream()
                            .map(id -> {
                                checkListRepository.findById(id.longValue())
                                        .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));
                                return ProjectAddCheckListRequest.Converter
                                        .toEntity(savedProject, id.longValue());
                            })
                            .toList();

            projectChecklistRepository.saveAll(projectCheckLists);
        }
        return ProjectCreateResponse.Converter.from(savedProject);
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

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(Pageable pageable, String searchKeyword, Boolean isDeleted) {

        // 1. 검색 조건에 따른 Repository 메서드 호출
        Page<Project> projectPage;
        Boolean deletedStatus = (isDeleted != null) ? isDeleted : false;

        if (searchKeyword != null && !searchKeyword.isBlank()) {
            projectPage = projectRepository.findByProjectNameContainingIgnoreCaseAndIsDeleted(
                    searchKeyword, deletedStatus, pageable
            );
        } else {
            projectPage = projectRepository.findByIsDeleted(deletedStatus, pageable);
        }

        List<Project> projects = projectPage.getContent();

        // 2. Member 정보 일괄 조회
        List<Long> projectIds = projects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());

        // 프로젝트 ID 목록을 기반으로 모든 관련 ProjectMember를 한 번에 조회
        List<ProjectMember> allMembers = projectMemberRepository.findByProjectIdIn(projectIds);

        // 3. Project ID별 ProjectMember Map 생성
        Map<Long, List<ProjectMember>> membersByProjectId = allMembers.stream()
                .collect(Collectors.groupingBy(pm -> pm.getProject().getId()));

        // 4. Page<Project>를 Page<ProjectResponse>로 변환
        return projectPage.map(project -> {
            List<ProjectMember> members = membersByProjectId.getOrDefault(project.getId(), List.of());
            return ProjectResponse.Converter.from(project, members);
        });
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
                .memoType(MemoType.MAIN)
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

        // 2. 이미 프로젝트에 있는 멤버들의 userId 조회
        List<ProjectMember> existingMembers = projectMemberRepository.findByProjectId(project.getId());
        List<Long> existingUserIds = existingMembers.stream()
                .map(pm -> pm.getUser().getId())
                .toList();

        // 3. userId로 User 엔티티 일괄 조회
        List<User> users = userRepository.findAllById(memberIds);

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 4. 존재하는 User이면서 프로젝트에 아직 없는 멤버만 필터링하여 ProjectMember 생성
        List<ProjectMember> projectMembers = members.stream()
                .filter(req -> userMap.containsKey(req.getUserId()))
                .filter(req -> !existingUserIds.contains(req.getUserId())) // 이미 있는 멤버 제외
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
                            .orElseThrow(() -> new BusinessException(ErrorCode.CHECKLIST_NOT_FOUND));

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
    public ProjectDetailResponse getProjectDetail(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        Optional<Memo> projectMemo = memoRepository.findByProjectId(projectId, MemoType.MAIN);

        Optional<Memo> userMemo = memoRepository.findByUserIdAndProjectId(userId, projectId, MemoType.USER);

        return ProjectDetailResponse.Converter.from(project, projectMemo.orElse(null), userMemo.orElse(null));
    }

    // 삭제된 프로젝트 복원
    @Override
    @Transactional
    public ProjectRestoreResponse restoreDeletedProject(Long loginUserId, ProjectRestoreRequest request) {

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

    // 삭제된 프로젝트 영구삭제
    @Override
    @Transactional
    public ProjectHardDeleteResponse hardDeleteProject(Long loginUserId, @Valid ProjectHardDeleteRequest request){

        // 1. 삭제 요청한 프로젝트 ID 목록 조회
        List<Long> projectIds = request.getProjectIds();
        List<Project> projects = projectRepository.findAllById(projectIds);

        // 2. 존재 개수 비교
        if (projects.size() != projectIds.size()) {
            throw new ProjectNotFoundException("존재하지 않는 프로젝트가 포함되어 있습니다.");
        }

        // 3. 삭제 여부 체크
        projects.forEach(project -> {
            if (!project.getIsDeleted()) {
                throw new BusinessException(ErrorCode.PROJECT_NOT_DELETED);}
        });

        // 4. S3에서 삭제할 파일 목록 조회
        List<File> files = fileRepository.findByProjectIds(projectIds);

        // 5. S3 파일 먼저 삭제 (실패 시 예외 발생하여 트랜잭션 롤백)
        fileService.deleteFilesFromS3(files);

        // 6. DB 에서 프로젝트 삭제 (트랜잭션으로 보호, Cascade로 연관 엔티티도 삭제)
        projectRepository.deleteAll(projects);

        return ProjectHardDeleteResponse.Converter.from(projects);
    }

    // 프로젝트 이미지 수정
    @Override
    @Transactional
    public ProjectUpdateResponse updateProjectImage(Long projectId, MultipartFile image) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // 이미지가 있을 경우 S3에 업로드하고 URL 받기
        if (image != null && !image.isEmpty()) {
            // S3 업로드
            String imageUrl = fileService.uploadToS3(image).getFileUrl();
            // 프로젝트에 이미지 주소 저장
            project.setProjectImageUrl(imageUrl);
            projectRepository.save(project);
        }
        return ProjectUpdateResponse.Converter.from(project);
    }

    // 프로젝트 체크리스트 삭제
    @Override
    public void checklistDeleted(Long projectId, Long checkListId) {
        // 프로젝트 체크리스트 존재 확인
        ProjectCheckList projectCheckList = projectChecklistRepository.findByProject_IdAndCheckListId(projectId, checkListId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_CHECKLIST_NOT_FOUND));

        // 파일 존재 확인
        List<File> files = fileRepository.findByProjectCheckListId(projectCheckList.getProjectCheckListId());

        // S3 파일 삭제
        fileService.deleteFilesFromS3(files);

        // 프로젝트 체크리스트 삭제 및 파일과 링크 자동 삭제
        projectChecklistRepository.delete(projectCheckList);
    }
}

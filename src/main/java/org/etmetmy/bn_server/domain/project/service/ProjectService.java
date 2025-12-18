package org.etmetmy.bn_server.domain.project.service;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ProjectListResponse;
import org.etmetmy.bn_server.domain.project.dto.request.*;
import org.etmetmy.bn_server.domain.project.dto.response.*;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectMemberRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectService {

    ProjectCreateResponse createProject(ProjectCreateRequest request, Long loginUserId);

    int addProjectMembers(Long projectId, List<ProjectMemberRequest> members, Long createdById);

    List<ProjectResponse> getAllProjects();
    List<ProjectCustomerResponse> getAllProjects(Long loginUserId);
    List<ProjectMemberResponse> getProjectMembers(Long projectId);

    //프로젝트 리스트 페이지네이션, 검색
    Page<ProjectResponse> getProjects(Pageable pageable, String searchKeyword, Boolean isDeleted);

    ProjectResponse getProjectById(Long projectId);
    List<ProjectAddCheckListResponse> checklistAdd(Long projectId, ProjectAddCheckListRequest request);

    // 프로젝트 제목 수정
    ProjectUpdateResponse updateProjectName(Long projectId, ProjectNameUpdateRequest request);

    // 프로젝트 날짜 수정
    ProjectUpdateResponse updateProjectDate(Long projectId, ProjectDateUpdateRequest request);

    //프로젝트 삭제(흊지통이동)
    ProjectTrashResponse deleteProject(Long projectId);

    // 프로젝트 멤버 삭제
    void removeProjectMember(Long projectId, Long userId);

    List<ProjectCheckListAllResponse> getCheckLists(Long projectId);

    // 프로젝트 진행단계 수정
    ProjectStageUpdateResponse updateProjectStage(Long projectId, ProjectStageUpdateRequest request, Long currentUserId);
    ProjectDetailResponse getProjectDetail(Long userId, Long projectId);

    // 삭제된 프로젝트 복원
    ProjectRestoreResponse restoreDeletedProject(Long loginUserId, ProjectRestoreRequest request);

    // 프로젝트 이미지 수정
    ProjectUpdateResponse updateProjectImage(Long projectId, MultipartFile image);

    // 프로젝트 체크리스트 삭제
    void checklistDeleted(Long projectId, Long checkListId);

    // 프로젝트 영구삭제
    ProjectHardDeleteResponse hardDeleteProject(Long loginUserId, @Valid ProjectHardDeleteRequest request);

    //개별 프로젝트에 바로 체크리스트 생성
    ProjectCreateCheckListResponse createAndAddCheckList(Long projectId, ProjectCreateCheckListRequest request);
}

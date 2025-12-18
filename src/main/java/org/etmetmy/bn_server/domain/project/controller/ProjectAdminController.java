package org.etmetmy.bn_server.domain.project.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.activityLog.aop.ActivityLogger;
import org.etmetmy.bn_server.domain.project.dto.request.*;
import org.etmetmy.bn_server.domain.project.dto.response.*;

import org.etmetmy.bn_server.domain.project.service.ProjectMemberService;
import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.etmetmy.bn_server.web.SessionConst;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Project (Admin)", description = "관리자 프로젝트 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/projects")
public class ProjectAdminController {

    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;

    // todo: 프로젝트 생성
    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다")
    @ActivityLogger(action = "CREATE", targetType = "Project")
    @PostMapping(value = "/api/v1/admin/projects", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<ProjectCreateResponse> createProject(
            HttpSession session,
            @RequestBody @Valid ProjectCreateRequest request
    ) {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        ProjectCreateResponse response = projectService.createProject(request, loginUserId);
        return CommonResponse.success("프로젝트가 생성되었습니다.", response);
    }

    // todo : 프로젝트 이미지 업로드
    @PostMapping(value = "/api/v1/admin/projects/{projectId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<ProjectUpdateResponse> uploadImage(
            @PathVariable Long projectId,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        ProjectUpdateResponse response = projectService.updateProjectImage(projectId, image);
        return CommonResponse.success("프로젝트 이미지 업로드 성공", response);
    }


    // todo: 프로젝트 전체 조회 (페이지네이션, 검색, 삭제여부)
    @Operation(summary = "프로젝트 전체 조회(페이징, 검색, 삭제여부)", description = "모든 프로젝트 목록을 조회합니다")
    @GetMapping
    public CommonResponse<Page<ProjectResponse>> getProjects(

            @Parameter(hidden = true)
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC, page = 0, size = 10)
            Pageable pageable,

            @Parameter(description = "검색 키워드 (프로젝트 이름)")
            @RequestParam(required = false) String searchKeyword, // 쉼표 추가 및 메서드 바디에서 분리


            @Parameter(description = "삭제된 프로젝트 포함 여부 (true: 삭제된 프로젝트만 조회, false 또는 미입력: 삭제되지 않은 프로젝트만 조회)")
            @RequestParam(required = false) Boolean isDeleted) {

        Page<ProjectResponse> responses = projectService.getProjects(pageable, searchKeyword, isDeleted);

        return CommonResponse.success("프로젝트 목록조회 성공", responses);
    }

    // todo: 개별 프로젝트 멤버 조회
    @Operation(summary = "프로젝트 멤버 조회", description = "특정 프로젝트의 멤버 목록을 조회합니다")
    @GetMapping("/{projectId}/members")
    public CommonResponse<List<ProjectMemberResponse>> getProjectMembers(@PathVariable Long projectId) {
        List<ProjectMemberResponse> responses = projectService.getProjectMembers(projectId);

        return CommonResponse.success("프로젝트 멤버 조회 성공", responses);
    }

    // todo: 개별 프로젝트 멤버 추가
    @Operation(summary = "프로젝트 멤버 추가", description = "프로젝트에 새로운 멤버를 추가합니다")
    @PostMapping("/{projectId}/members")
    public CommonResponse<Integer> addProjectMembers(
            HttpSession session,
            @PathVariable Long projectId,
            @RequestBody List<ProjectMemberRequest> requests
    ) {
        Long currentUserId = (Long) session.getAttribute("userId");
        int addedCount = projectService.addProjectMembers(projectId, requests, currentUserId);
        return CommonResponse.success("프로젝트 멤버 추가 성공", addedCount);
    }

    // todo: 개별프로젝트에 체크리스트 추가
    @Operation(summary = "프로젝트 체크리스트 추가", description = "프로젝트에 체크리스트를 할당합니다")
    @PostMapping("/{projectId}/checklists")
    public CommonResponse<List<ProjectAddCheckListResponse>> addCheckLists(@PathVariable Long projectId,
                                                                           @RequestBody ProjectAddCheckListRequest request) {
        return CommonResponse.success("체크리스트를 할당했습니다", projectService.checklistAdd(projectId, request));

    }

    // todo: 개별프로젝트에 체크리스트 삭제
    @Operation(summary = "프로젝트 체크리스트 삭제", description = "프로젝트에 체크리스트를 삭제")
    @DeleteMapping("/{projectId}/checklists/{checkListId}")
    public CommonResponse<Object> addCheckLists(@PathVariable Long projectId,
                                                @PathVariable Long checkListId) {
        projectService.checklistDeleted(projectId, checkListId);
        return CommonResponse.success("체크리스트를 삭제했습니다", null);
    }

    // Todo : 개별 프로젝트 체크리스트 전체 조회
    @Operation(summary = "프로젝트 체크리스트 조회", description = "프로젝트의 모든 체크리스트를 조회합니다")
    @GetMapping("/{projectId}/checklists")
    public CommonResponse<List<ProjectCheckListAllResponse>> getCheckLists(
            @PathVariable Long projectId
    ) {
        return CommonResponse.success("프로젝트 체크리스트를 불러왔습니다.", projectService.getCheckLists(projectId));
    }

    // todo: 개별 프로젝트 제목 수정
    @Operation(summary = "프로젝트 제목 수정", description = "프로젝트의 제목을 수정합니다")
    @ActivityLogger(action = "UPDATE", targetType = "Project")
    @PatchMapping("/{projectId}/projectName")
    public CommonResponse<ProjectUpdateResponse> updateProjectName(
            @PathVariable Long projectId,
            @RequestBody ProjectNameUpdateRequest request
    ) {
        ProjectUpdateResponse response = projectService.updateProjectName(projectId, request);
        return CommonResponse.success("프로젝트 제목 수정 성공", response);
    }

    // todo: 프로젝트 이미지 수정
    @Operation(summary = "프로젝트 이미지 수정", description = "프로젝트의 이미지를 수정합니다")
    @ActivityLogger(action = "UPDATE", targetType = "Project")
    @PatchMapping(value = "/{projectId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<ProjectUpdateResponse> updateProjectImage(
            @PathVariable Long projectId,
            @RequestPart("image") MultipartFile image
    ) {
        ProjectUpdateResponse response = projectService.updateProjectImage(projectId, image);
        return CommonResponse.success("프로젝트 이미지 수정 성공", response);
    }

    // todo: 개별 프로젝트 날짜 수정
    @Operation(summary = "프로젝트 날짜 수정", description = "프로젝트의 시작일 및 종료일을 수정합니다")
    @ActivityLogger(action = "UPDATE", targetType = "Project")
    @PatchMapping("/{projectId}/date")
    public CommonResponse<ProjectUpdateResponse> updateProjectDate(
            @PathVariable Long projectId,
            @RequestBody ProjectDateUpdateRequest request
    ) {
        ProjectUpdateResponse response = projectService.updateProjectDate(projectId, request);
        return CommonResponse.success("프로젝트 날짜 수정 성공", response);
    }

    // todo: 개별 프로젝트 soft 삭제 (휴지통으로 이동)
    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 휴지통으로 이동합니다 (soft delete)")
    @DeleteMapping("/{projectId}")
    @ActivityLogger(action = "DELETE", targetType = "Project")
    public CommonResponse<ProjectTrashResponse> deleteProject(@PathVariable Long projectId) {
        ProjectTrashResponse response = projectService.deleteProject(projectId); // ✅ 인스턴스 사용
        return CommonResponse.success("프로젝트 휴지통 이동 완료", response);
    }

    // todo: 개별 프로젝트 멤버 삭제
    @Operation(summary = "프로젝트 멤버 삭제", description = "프로젝트에서 멤버를 제거합니다")
    @DeleteMapping("/{projectId}/members/{userId}")
    public CommonResponse<Long> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId
    ) {
        projectService.removeProjectMember(projectId, userId);
        return CommonResponse.success("프로젝트 멤버 삭제 성공", projectId);
    }

    // todo: 프로젝트 진행단계 수정
    @Operation(summary = "프로젝트 진행단계 수정", description = "프로젝트의 진행단계를 수정합니다")
    @PatchMapping("/{projectId}/stage")
    @ActivityLogger(action = "UPDATE", targetType = "Project")
    public CommonResponse<ProjectStageUpdateResponse> updateProjectStage(
            HttpSession session,
            @PathVariable Long projectId,
            @RequestBody ProjectStageUpdateRequest request
    ) {
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            Object loginMember = session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember instanceof User user) {
                currentUserId = user.getId();
            }
        }

        ProjectStageUpdateResponse response =
                projectService.updateProjectStage(projectId, request, currentUserId);

        return CommonResponse.success("프로젝트 진행단계 수정 성공", response);
    }

    // todo: 개별 프로젝트 조회
    @Operation(summary = "프로젝트 상세 조회", description = "특정 프로젝트의 상세 정보를 조회합니다")
    @GetMapping("/{projectId}")
    public CommonResponse<ProjectDetailResponse> getProjectDetail(
            @PathVariable Long projectId,
            HttpSession session
    ) {

        Long userId = SessionUtil.getLoginUserId(session);

        ProjectDetailResponse response = projectService.getProjectDetail(userId, projectId);
        return CommonResponse.success("프로젝트 조회 성공", response);
    }

    // todo: 프로젝트 생성 - 개발사/고객사 사원 조회
    @Operation(summary = "프로젝트 생성용 사원 조회", description = "프로젝트 생성 시 추가 가능한 개발사/고객사 사원을 조회합니다")
    @GetMapping(value = "/users")
    public CommonResponse<List<ProjectMemberSearchResponse>> searchUsersForCreate(@RequestParam("role") Role role) {
        List<ProjectMemberSearchResponse> responses = projectMemberService.searchUsersForCreate(role);
        return CommonResponse.success("프로젝트 생성 사원 조회 성공", responses);

    }

    // todo: 프로젝트 설정 - 개별사/고객사 사원 조회
    @Operation(summary = "프로젝트 설정용 사원 조회", description = "프로젝트 설정 시 추가 가능한 개발사/고객사 사원을 조회합니다")
    @GetMapping("/{projectId}/users")
    public CommonResponse<List<ProjectMemberSearchResponse>> searchUsersForProject(@PathVariable Long projectId, @RequestParam("role") Role role) {
        List<ProjectMemberSearchResponse> responses = projectMemberService.searchUsersForProject(projectId, role);
        return CommonResponse.success("프로젝트 설정 사원 조회 성공", responses);
    }

    //todo: 삭제된 프로젝트 복원
    @Operation(summary = "프로젝트 복원", description = "휴지통에 있는 프로젝트를 복원합니다")
    @PatchMapping("/trash/restore")
    @ActivityLogger(action = "UPDATE", targetType = "Project")
    public CommonResponse<ProjectRestoreResponse> restoreDeletedProject(
            HttpSession session,
            @Valid @RequestBody ProjectRestoreRequest request) {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        ProjectRestoreResponse response = projectService.restoreDeletedProject(loginUserId, request);

        return CommonResponse.success("삭제된 프로젝트 복원 성공", response);
    }
}

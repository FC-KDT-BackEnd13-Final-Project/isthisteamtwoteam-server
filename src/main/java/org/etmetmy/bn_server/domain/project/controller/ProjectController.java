package org.etmetmy.bn_server.domain.project.controller;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.request.*;
import org.etmetmy.bn_server.domain.project.dto.response.*;

import org.etmetmy.bn_server.domain.project.service.ProjectMemberService;
import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.exception.custom.InvalidInputException;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.etmetmy.bn_server.web.SessionConst;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;

    @PostMapping
    public CommonResponse<Long> createProject(HttpSession session, @RequestBody ProjectCreateRequest request) {
        Long currentUserId = (Long) session.getAttribute("userId");

        Long projectId = projectService.createProject(request, currentUserId);

        ProjectResponse response = projectService.getProjectById(projectId);

        return CommonResponse.success("프로젝트 생성 성공", projectId);
    }

    //생성한 프로젝트 저장되어 노출
    @GetMapping
    public CommonResponse<List<ProjectResponse>> getProjects(){
        List<ProjectResponse> responses = projectService.getAllProjects();

        return CommonResponse.success("프로젝트 목록조회 성공", responses);
    }

    //프로젝트 멤버 저장 확인용
    @GetMapping("/{projectId}/members")
    public CommonResponse<List<ProjectMemberResponse>> getProjectMembers(@PathVariable Long projectId){
        List<ProjectMemberResponse> responses = projectService.getProjectMembers(projectId);

        return CommonResponse.success("프로젝트 멤버 조회 성공", responses);
    }

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

    @PostMapping("/{projectId}/checklists")
    public CommonResponse<List<ProjectAddCheckListResponse>> addCheckLists(@PathVariable Long projectId,
                                                                           @RequestBody ProjectAddCheckListRequest request) {
        return CommonResponse.success("체크리스트를 할당했습니다", projectService.checklistAdd(projectId, request));

    }

    // Todo : 단일 프로젝트 체크리스트 전체 조회
    @GetMapping("/{projectId}/checklists")
    public CommonResponse<List<ProjectCheckListAllResponse>> getCheckLists(
            @PathVariable Long projectId
    ){
        return CommonResponse.success("프로젝트 체크리스트를 불러왔습니다.",projectService.getCheckLists(projectId));
    }

    // 프로젝트 제목 수정
    @PatchMapping("/{projectId}/projectName")
    public CommonResponse<ProjectUpdateResponse> updateProjectName(
            @PathVariable Long projectId,
            @RequestBody ProjectNameUpdateRequest request
    ) {
        ProjectUpdateResponse response = projectService.updateProjectName(projectId, request);
        return CommonResponse.success("프로젝트 제목 수정 성공", response);
    }

    // 프로젝트 날짜 수정
    @PatchMapping("/{projectId}/date")
    public CommonResponse<ProjectUpdateResponse> updateProjectDate(
            @PathVariable Long projectId,
            @RequestBody ProjectDateUpdateRequest request
    ) {
        ProjectUpdateResponse response = projectService.updateProjectDate(projectId, request);
        return CommonResponse.success("프로젝트 날짜 수정 성공", response);
    }

    //프로젝트 삭제 (휴지통으로 이동)
    @DeleteMapping("/{projectId}")
    public CommonResponse<ProjectTrashResponse> deleteProject(@PathVariable Long projectId) {
        ProjectTrashResponse response = projectService.deleteProject(projectId); // ✅ 인스턴스 사용
        return CommonResponse.success("프로젝트 휴지통 이동 완료", response);
    }

    // 프로젝트 멤버 삭제
    @DeleteMapping("/{projectId}/members/{userId}")
    public CommonResponse<Long> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId
    ) {
        projectService.removeProjectMember(projectId, userId);
        return CommonResponse.success("프로젝트 멤버 삭제 성공", projectId);
    }


    // 프로젝트 진행단계 수정
    @PatchMapping("/{projectId}/stage")
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

    @GetMapping("/{projectId}")
    public CommonResponse<ProjectDetailResponse> getProjectDetail(
            @PathVariable Long projectId
    ) {
        ProjectDetailResponse response = projectService.getProjectDetail(projectId);
        return CommonResponse.success("프로젝트 조회 성공", response);
    }


    //프로젝트 생성 - 개발사/고객사 사원 조회
    @GetMapping(value = "/users" )
    public CommonResponse<List<ProjectMemberSearchResponse>> searchUsersForCreate(
            @RequestParam("role") Role role
    ) {
        if (role == null) {
            throw new InvalidInputException("role 파라미터는 필수입니다. (DEVELOPER 또는 CUSTOMER)");
        }

        if (role == Role.ADMIN) {
            throw new InvalidInputException("관리자(ADMIN)는 조회할 수 없습니다.");
        }

        List<ProjectMemberSearchResponse> responses;

        if (role == Role.DEVELOPER) {
            // 개발사: 회사명 NULL, 이미 프로젝트에 속한 유저 제외
            responses = projectMemberService.searchDeveloperMembers();
            return CommonResponse.success("개발사 사원 조회 성공", responses);
        } else if (role == Role.CUSTOMER) {
            // 고객사: 회사명 포함, 이미 프로젝트에 속한 유저 제외
            responses = projectMemberService.searchClientMembers();
            return CommonResponse.success("고객사 사원 조회 성공", responses);
        } else {
            throw new InvalidInputException("유효하지 않은 role 값입니다. (DEVELOPER, CUSTOMER만 사용 가능)");
        }
    }


    // 프로젝트 설정 - 개발사/고객사 담당자, 사원 조회
    @GetMapping("/{projectId}/users")
    public CommonResponse<List<ProjectMemberSearchResponse>> searchUsersForProject(
            @PathVariable Long projectId,
            @RequestParam("role") Role role
    ) {
        if (role == null) {
            throw new InvalidInputException("role 파라미터는 필수입니다. (DEVELOPER 또는 CUSTOMER)");
        }

        if (role == Role.ADMIN) {
            throw new InvalidInputException("관리자(ADMIN)는 조회할 수 없습니다.");
        }

        List<ProjectMemberSearchResponse> responses;

        if (role == Role.DEVELOPER) {
            // 개발사: 회사명 NULL, 이미 프로젝트에 속한 유저 제외
            responses = projectMemberService.searchDeveloperMembersForProject(projectId);
            return CommonResponse.success("개발사 사원 조회 성공", responses);
        } else if (role == Role.CUSTOMER) {
            // 고객사: 회사명 포함, 이미 프로젝트에 속한 유저 제외
            responses = projectMemberService.searchClientMembersForProject(projectId);
            return CommonResponse.success("고객사 사원 조회 성공", responses);
        } else {
            throw new InvalidInputException("유효하지 않은 role 값입니다. (DEVELOPER, CUSTOMER만 사용 가능)");
        }
    }
}

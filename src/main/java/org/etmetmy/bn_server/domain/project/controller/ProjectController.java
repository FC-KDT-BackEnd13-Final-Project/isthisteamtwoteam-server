package org.etmetmy.bn_server.domain.project.controller;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.request.*;
import org.etmetmy.bn_server.domain.project.dto.response.*;

import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public CommonResponse<Long> createProject(HttpSession session, @RequestBody ProjectCreateRequest request) {
        Long currentUserId = (Long) session.getAttribute("userId");

        Long projectId = projectService.createProject(request, currentUserId);

        ProjectResponse response = projectService.getProjectById(projectId);

        return CommonResponse.success("프로젝트 생성 성공", projectId);
    }

    //생성한 프로젝트 저장되어 노출
    //CommonResponse 적용
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
        Long currentUserId = (Long) session.getAttribute("userId"); // 🔥 로그인한 사용자 ID

        ProjectStageUpdateResponse response =
                projectService.updateProjectStage(projectId, request, currentUserId);

        return CommonResponse.success("프로젝트 진행단계 수정 성공", response);
    }
}

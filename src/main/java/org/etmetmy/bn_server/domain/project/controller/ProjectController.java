package org.etmetmy.bn_server.domain.project.controller;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.request.*;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectAddCheckListResponse;

import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @PatchMapping("/{projectId}/title")
    public CommonResponse<Map<String, Object>> updateProjectTitle(
            @PathVariable Long projectId,
            @RequestBody ProjectTitleUpdateRequest request
    ) {
        projectService.updateProjectTitle(projectId, request);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("project_id", projectId);

        return CommonResponse.success("프로젝트 제목 수정 성공", responseData);
    }

    // 프로젝트 날짜 수정
    @PatchMapping("/{projectId}/date")
    public CommonResponse<Map<String, Object>> updateProjectDate(
            @PathVariable Long projectId,
            @RequestBody ProjectDateUpdateRequest request
    ) {
        projectService.updateProjectDate(projectId, request);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("project_id", projectId);

        return CommonResponse.success("프로젝트 날짜 수정 성공", responseData);
    }

}

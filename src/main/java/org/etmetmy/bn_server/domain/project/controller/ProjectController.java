package org.etmetmy.bn_server.domain.project.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectMemberRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
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




}

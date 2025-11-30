package org.etmetmy.bn_server.domain.project.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
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
        Long currentUserId = getCurrentUserId();   // TODO: Security 연동 시 수정


        Long projectId = projectService.createProject(request, currentUserId);

        List<ProjectMemberResponse> members = projectService.getProjectMembers(projectId);

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

    //임시
    private Long getCurrentUserId() {
        return 1L;
    }
}

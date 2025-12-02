package org.etmetmy.bn_server.domain.project.controller;


import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectAddCheckListRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectAddCheckListResponse;
import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/{projectId}/checklists")
    public CommonResponse<List<ProjectAddCheckListResponse>> addCheckLists(@PathVariable Long projectId,
                                                                           @RequestBody ProjectAddCheckListRequest request) {
        return CommonResponse.success("체크리스트를 할당했습니다", projectService.checklistAdd(projectId, request));
    }
}

package org.etmetmy.bn_server.domain.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.dto.request.UpdateStageRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectCustomerResponse;
import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project (Customer)", description = "진용이가 만든 프로젝트 대시보드 넘어가기 API")
@RestController
@RequiredArgsConstructor
public class ProjectJinyongController {
    private final ProjectService projectService;

    @Operation(summary = "진용이api", description = "진용진용진용진용진용진용진용진용진용진용진용진용진용진용진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진진용진용진용진용진용진용진용진용진용진용진용")
    @PatchMapping("/api/v1/developer/project/{projectId}/dashboard/stage")
    public CommonResponse<Object> getAllProjects(HttpSession session,  @PathVariable Long projectId, @RequestBody UpdateStageRequest request
    ) {
        projectService.updateProjectStage(projectId, request.getStageName());
        return CommonResponse.success("진용이 스테이지 넘어가기 기능 성공 ><");

    }


}

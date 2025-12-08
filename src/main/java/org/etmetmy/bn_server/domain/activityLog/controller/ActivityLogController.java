package org.etmetmy.bn_server.domain.activityLog.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.etmetmy.bn_server.global.util.RequestUtil;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.activityLog.dto.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.service.ActivityLogService;
import org.etmetmy.bn_server.domain.activityLog.service.ActivityLogServiceImpl;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/projects")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    // 프로젝트 활동 로그들 출력
    @GetMapping("/{projectId}/logs")
    public CommonResponse<List<ActivityLogResponse>> getProjectActivityLogs(
            @PathVariable Long projectId
    ) {
        List<ActivityLogResponse> logs = activityLogService.getProjectLogs(projectId);
        return CommonResponse.success("프로젝트 활동 로그를 가져왔습니다.",logs);
    }

    //IP 주소 가져오기
}
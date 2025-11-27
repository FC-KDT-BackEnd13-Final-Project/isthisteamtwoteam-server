package org.etmetmy.bn_server.domain.activityLog.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.activityLog.dto.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects") //
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    // GET /api/projects/{projectId}/logs
    @GetMapping("/{projectId}/logs")
    public ResponseEntity<List<ActivityLogResponse>> getProjectActivityLogs(
            @PathVariable Long projectId
    ) {
        List<ActivityLogResponse> logs = activityLogService.getProjectLogs(projectId);
        return ResponseEntity.ok(logs);
    }
}
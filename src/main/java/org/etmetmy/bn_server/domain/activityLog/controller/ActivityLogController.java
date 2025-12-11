package org.etmetmy.bn_server.domain.activityLog.controller;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.activityLog.dto.response.ActivityLogResponse;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.activityLog.service.ActivityLogService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    // Todo: 프로젝트별 활동 로그 조회 (최신순, 페이지네이션)
    // GET /api/v1/projects/{projectId}/logs

    @GetMapping("/projects/{projectId}/logs")
    public CommonResponse<Page<ActivityLogResponse>> getProjectLogs(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogResponse> logs = activityLogService.getProjectLogsWithPaging(projectId, pageable);
        return CommonResponse.success("활동 로그 조회 성공", logs);
    }


    // Todo: 액션 타입 필터
    //GET /api/v1/projects/{projectId}/logs/by-action?action=CREATE

    @GetMapping("/projects/{projectId}/logs/by-action")
    public CommonResponse<List<ActivityLogResponse>> getProjectLogsByAction(
            @PathVariable Long projectId,
            @RequestParam ActivityAction action
    ) {
        List<ActivityLogResponse> logs = activityLogService.getProjectLogsByAction(projectId, action);
        return CommonResponse.success("활동 로그 조회 성공 (액션 필터)", logs);
    }

    //Todo: 사용자 필터
    //GET /api/v1/projects/{projectId}/logs/by-user?userId=1

    @GetMapping("/projects/{projectId}/logs/by-user")
    public CommonResponse<List<ActivityLogResponse>> getProjectLogsByUser(
            @PathVariable Long projectId,
            @RequestParam Long userId
    ) {
        List<ActivityLogResponse> logs = activityLogService.getProjectLogsByUser(projectId, userId);
        return CommonResponse.success("활동 로그 조회 성공 (사용자 필터)", logs);
    }

    //Todo: 기간 필터
    //GET /api/v1/projects/{projectId}/logs/by-date?startDate=2025-12-01&endDate=2025-12-3

    @GetMapping("/projects/{projectId}/logs/by-date")
    public CommonResponse<List<ActivityLogResponse>> getLogsByDateRange(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // LocalDate를 LocalDateTime으로 변환
        LocalDateTime startDateTime = startDate.atStartOfDay();           // 2025-12-11 00:00:00
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // 2025-12-11 23:59:59

        List<ActivityLogResponse> logs = activityLogService.getProjectLogsByDateRange(
                projectId, startDateTime, endDateTime);
        return CommonResponse.success("활동 로그 조회 성공", logs);
    }

    //Todo: 전체 활동 로그 조회
    //GET /api/v1/admin/activity-logs?page=0&size=50
    @GetMapping("/admin/activity-logs")
    public CommonResponse<Page<ActivityLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ActivityLogResponse> logs = activityLogService.getAllLogsWithPaging(pageable);
        return CommonResponse.success("전체 활동 로그 조회 성공", logs);
    }
}
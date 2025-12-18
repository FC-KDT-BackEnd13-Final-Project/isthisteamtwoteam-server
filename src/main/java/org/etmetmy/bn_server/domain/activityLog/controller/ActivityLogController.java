package org.etmetmy.bn_server.domain.activityLog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Activity Log", description = "활동 로그 조회 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    /**
     * 프로젝트별 활동 로그 조회 (통합 필터링)
     * GET /api/v1/projects/{projectId}/logs?action=CREATE&userId=1&startDate=2025-12-01&page=0&size=20
     */
    @Operation(
            summary = "프로젝트 활동 로그 조회 (통합 필터링)",
            description = "특정 프로젝트의 활동 로그를 다양한 조건으로 필터링하여 조회합니다"
    )
    @GetMapping("/projects/{projectId}/logs")
    public CommonResponse<Page<ActivityLogResponse>> getProjectLogs(
            @PathVariable Long projectId,

            @Parameter(description = "액션 타입 필터 (CREATE, UPDATE, DELETE 등)")
            @RequestParam(required = false) ActivityAction action,

            @Parameter(description = "사용자 ID 필터")
            @RequestParam(required = false) Long userId,

            @Parameter(description = "시작일 (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "종료일 (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size
    ) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ActivityLogResponse> logs = activityLogService.getProjectLogsWithFilters(
                projectId, action, userId, startDateTime, endDateTime, pageable
        );

        return CommonResponse.success("활동 로그 조회 성공", logs);
    }

    /**
     * 전체 활동 로그 조회 (관리자용)
     * GET /api/v1/admin/activity-logs?page=0&size=50
     */
    @Operation(
            summary = "전체 활동 로그 조회",
            description = "모든 프로젝트의 활동 로그를 조회합니다 (관리자용)"
    )
    @GetMapping("/admin/activity-logs")
    public CommonResponse<Page<ActivityLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ActivityLogResponse> logs = activityLogService.getAllLogsWithPaging(pageable);
        return CommonResponse.success("전체 활동 로그 조회 성공", logs);
    }

    /**
     * 특정 대상의 활동 로그 조회
     * GET /api/v1/projects/{projectId}/logs/target?targetType=Post&targetId=123
     */
    @Operation(
            summary = "특정 대상의 활동 이력 조회",
            description = "특정 게시글, 체크리스트 등의 모든 변경 이력을 조회합니다"
    )
    @GetMapping("/projects/{projectId}/logs/target")
    public CommonResponse<Page<ActivityLogResponse>> getTargetLogs(
            @PathVariable Long projectId,
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ActivityLogResponse> logs = activityLogService.getTargetLogs(
                projectId, targetType, targetId, pageable
        );
        return CommonResponse.success("대상 활동 이력 조회 성공", logs);
    }
}
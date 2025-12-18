package org.etmetmy.bn_server.domain.activityLog.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ActivityLogResponse {

    private Long logId;

    // 프로젝트 정보
    private Long projectId;
    private String projectName;

    // 사용자 정보
    private Long userId;
    private String userName;

    // 활동 정보
    private String action;           // "생성", "수정", "삭제" 등
    private String actionCode;       // "CREATE", "UPDATE", "DELETE" (필터링용)
    private String targetType;       // "Post", "Project", "Comment" 등
    private Long targetId;

    // 상세 설명
    private String description;      // "홍길동님이 '프로젝트 A'를 생성했습니다"

    // 메타 정보
    private String ipAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static class Converter {

        public static ActivityLogResponse from(ActivityLog log) {
            return ActivityLogResponse.builder()
                    .logId(log.getLogId())
                    .projectId(log.getProjectId())
                    .projectName(log.getProjectName())
                    .userId(log.getUserId())
                    .userName(log.getUserName())
                    .action(log.getAction().getDescription())          // "생성"
                    .actionCode(log.getAction().name())                // "CREATE"
                    .targetType(log.getTargetType())
                    .targetId(log.getTargetId())
                    .description(log.getDescription())
                    .ipAddress(log.getIpAddress())
                    .createdAt(log.getCreatedAt())
                    .build();
        }

        public static List<ActivityLogResponse> fromList(List<ActivityLog> logs) {
            return logs.stream()
                    .map(ActivityLogResponse.Converter::from)
                    .toList();
        }
    }
}
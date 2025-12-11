package org.etmetmy.bn_server.domain.activityLog.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ActivityLogResponse {

    private Long logId;
    private Long userId;
    private String action;
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt;

    public static class Converter {
        // 엔티티 -> DTO 변환 편의 메서드
        public static ActivityLogResponse from(ActivityLog log) {
            return ActivityLogResponse.builder()
                    .logId(log.getLogId())
                    .userId(log.getUserId())
                    .action(log.getAction().getDescription()) // Enum의 한글 설명("수정", "생성") 반환
                    .targetType(log.getTargetType())
                    .targetId(log.getTargetId())
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
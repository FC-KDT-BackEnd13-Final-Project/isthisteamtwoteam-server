package org.etmetmy.bn_server.domain.activityLog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ActivityLogResponse {

    @JsonProperty("logId")
    private Long logId;

    private Long userId;
    private String action;
    private String targetType;    // 대상 (Post, Member)
    private Long targetId;        // 대상 ID

    @JsonProperty("ipAddress")
    private String ipAddress;

    //DB의 String -> List 객체
    private List<LogDetail> details;

    private LocalDateTime createdAt;

    public static class Converter {
        // 엔티티 -> DTO 변환 편의 메서드 (detail은 서비스에서 변환해서 넣어줌)
        public static ActivityLogResponse from(ActivityLog log, List<LogDetail> parsedDetails) {
            return ActivityLogResponse.builder()
                    .logId(log.getLogId())
                    .userId(log.getUserId())
                    .action(log.getAction().getDescription()) // Enum의 한글 설명("수정", "생성") 반환
                    .targetType(log.getTargetType())
                    .targetId(log.getTargetId())
                    .details(parsedDetails) // 파싱된 리스트 주입
                    .createdAt(log.getCreatedAt())
                    .ipAddress(log.getIpAddress())
                    .build();
        }
    }
}
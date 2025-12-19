package org.etmetmy.bn_server.domain.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryItemResponse {

    private String title;                  // "홍길동님이 2025-12-18 15:30에 게시글을 수정했습니다"
    private String targetType;             // POST, COMMENT, FILE, LINK
    private ChangeType changeType;         // CREATE, UPDATE, DELETE
    private LocalDateTime changedAt;       // 변경 시간
    private String changedByUserName;      // 변경한 사람 이름
    private String changeIp;               // 변경 IP
    private Object details;                // 상세 정보 (before/after 데이터 등)

    /**
     * 제목 생성 헬퍼 메서드
     * "누가, 언제, 무엇을, 어떻게" 형식으로 제목 생성
     */
    public static String createTitle(String userName, LocalDateTime changedAt, String targetType, ChangeType changeType) {
        String action = getActionText(targetType, changeType);
        String timeStr = changedAt.toString().replace("T", " ");
        return String.format("%s님이 %s에 %s", userName, timeStr, action);
    }

    /**
     * 변경 타입에 따른 동작 텍스트 반환
     */
    private static String getActionText(String targetType, ChangeType changeType) {
        String target = switch (targetType) {
            case "POST" -> "게시글을";
            case "COMMENT" -> "댓글을";
            case "FILE" -> "파일을";
            case "LINK" -> "링크를";
            default -> "항목을";
        };

        String action = switch (changeType) {
            case CREATE -> "추가했습니다";
            case UPDATE -> "수정했습니다";
            case DELETE -> "삭제했습니다";
        };

        return target + " " + action;
    }
}

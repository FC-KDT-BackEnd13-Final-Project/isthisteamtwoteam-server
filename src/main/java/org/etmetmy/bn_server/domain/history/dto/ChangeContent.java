package org.etmetmy.bn_server.domain.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 히스토리에서 변경된 내용만 표시하는 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeContent {

    private String type;        // 변경된 필드 타입 (title, content, stageName 등)
    private String before;      // 변경 전 내용
    private String after;       // 변경 후 내용

    /**
     * 변경 내용 생성 헬퍼 메서드 (수정)
     */
    public static ChangeContent of(String type, String before, String after) {
        return ChangeContent.builder()
                .type(type)
                .before(before)
                .after(after)
                .build();
    }

    /**
     * 삭제 시 변경 내용 생성
     */
    public static ChangeContent ofDelete(String type, String value) {
        return ChangeContent.builder()
                .type(type)
                .before(value)
                .after(null)
                .build();
    }

    /**
     * 생성 시 변경 내용 생성
     */
    public static ChangeContent ofCreate(String type, String value) {
        return ChangeContent.builder()
                .type(type)
                .before(null)
                .after(value)
                .build();
    }
}

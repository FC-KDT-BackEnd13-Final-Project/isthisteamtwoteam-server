package org.etmetmy.bn_server.domain.activityLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogDetail {
    private String field;      // 변경된 필드명 (예: "title", "status")
    private String oldValue;   // 변경 전 값 (예: "할일 목록")
    private String newValue;   // 변경 후 값 (예: "할일 목록 (완료)")
}
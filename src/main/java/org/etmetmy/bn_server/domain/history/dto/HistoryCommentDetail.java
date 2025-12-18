package org.etmetmy.bn_server.domain.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryCommentDetail {
    // 변경 전
    private String beContent;

    // 변경 후
    private String afContent;
}

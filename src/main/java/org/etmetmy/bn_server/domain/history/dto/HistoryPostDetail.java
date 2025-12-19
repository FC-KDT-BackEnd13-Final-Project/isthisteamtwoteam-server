package org.etmetmy.bn_server.domain.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryPostDetail {
    // 변경 전
    private String beTitle;
    private String beContent;
    private String beStageName;
    private Boolean beIsCompleted;

    // 변경 후
    private String afTitle;
    private String afContent;
    private String afStageName;
    private Boolean afIsCompleted;
}

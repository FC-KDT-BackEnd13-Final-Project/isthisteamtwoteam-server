package org.etmetmy.bn_server.domain.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryListResponse {
    private Long postId;
    private int totalCount;
    private List<HistoryItemResponse> histories;
}

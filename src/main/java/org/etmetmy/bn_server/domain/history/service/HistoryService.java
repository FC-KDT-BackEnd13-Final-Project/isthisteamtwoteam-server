package org.etmetmy.bn_server.domain.history.service;

import org.etmetmy.bn_server.domain.history.dto.HistoryListResponse;

public interface HistoryService {
    HistoryListResponse getAllHistory(Long postId);
}

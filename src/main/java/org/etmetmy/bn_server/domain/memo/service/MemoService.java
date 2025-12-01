package org.etmetmy.bn_server.domain.memo.service;

import org.etmetmy.bn_server.domain.memo.dto.response.MemoResponse;

import java.util.List;

public interface MemoService {
    List<MemoResponse> getProjectMemos(Long projectId);

    MemoResponse getUserMemo(Long userId, Long projectId);

    Long updateMemo(Long userId, Long projectId, String content);
}

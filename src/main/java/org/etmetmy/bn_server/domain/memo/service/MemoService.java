package org.etmetmy.bn_server.domain.memo.service;

import org.etmetmy.bn_server.domain.memo.dto.response.MemoResponse;

import java.util.List;

public interface MemoService {
    MemoResponse getProjectMemos(Long projectId);

    MemoResponse getUserMemo(Long userId, Long projectId);

    Long updateUserMemo(Long userId, Long projectId, String content);

    Long updateProjectMemo(Long userId, Long projectId, String content);
}

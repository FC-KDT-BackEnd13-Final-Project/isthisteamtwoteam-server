package org.etmetmy.bn_server.domain.memo.service;

import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.dto.entity.MemoResponse;

import java.util.List;

public interface MemoService {
    List<MemoResponse> getProjectMemos(Long projectId);

    Memo getUserMemo();
}

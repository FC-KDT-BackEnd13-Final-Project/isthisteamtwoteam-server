package org.etmetmy.bn_server.domain.memo.entity.service;

import org.etmetmy.bn_server.domain.memo.entity.dto.entity.MemoResponse;

import java.util.List;

public interface MemoService {
    List<MemoResponse> getProjectMemos(Long projectId);

}

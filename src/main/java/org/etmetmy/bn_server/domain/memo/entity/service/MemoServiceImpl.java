package org.etmetmy.bn_server.domain.memo.entity.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.entity.dto.entity.MemoResponse;
import org.etmetmy.bn_server.domain.memo.entity.repository.MemoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoServiceImpl implements MemoService{

    private final MemoRepository memoRepository;

    @Override
    public List<MemoResponse> getProjectMemos(Long projectId) {
        List<Memo> memos = memoRepository.findByProjectId(projectId);
        return MemoResponse.Converter.from(memos);
    }
}

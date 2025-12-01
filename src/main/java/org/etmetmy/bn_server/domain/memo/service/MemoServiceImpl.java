package org.etmetmy.bn_server.domain.memo.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.dto.response.MemoResponse;
import org.etmetmy.bn_server.domain.memo.repository.MemoRepository;
import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;
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

    @Override
    public MemoResponse getUserMemo(Long userId,Long projectId) {
        Memo memo = memoRepository.findByUserIdAndProjectId(userId,projectId)
                .orElse(null);

        if (memo == null) {
            return null;
        }

        return MemoResponse.Converter.from(memo);
    }

    //todo : 프로젝트 개인 메모 업데이트
    @Override
    public Long updateMemo(Long userId, Long projectId, String content) {
        Memo memo = memoRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new CustomException(StatusCode.MEMO_NOT_FOUND));

        // 더티체크 기능 활용
        memo.updateContent(content);

        Long memoId = memo.getMemoId();

        return memoId;
    }
}

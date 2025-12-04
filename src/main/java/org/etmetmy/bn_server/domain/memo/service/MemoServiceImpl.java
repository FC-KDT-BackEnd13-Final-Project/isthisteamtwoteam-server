package org.etmetmy.bn_server.domain.memo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.dto.response.MemoResponse;
import org.etmetmy.bn_server.domain.memo.entity.MemoType;
import org.etmetmy.bn_server.domain.memo.repository.MemoRepository;
import org.etmetmy.bn_server.domain.project.service.ProjectMemberService;

import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoServiceImpl implements MemoService{

    private final MemoRepository memoRepository;
    private final ProjectMemberService projectMemberService;

    //todo: 프로젝트 공용 메모 조회
    @Override
    @Transactional(readOnly = true)
    public MemoResponse getProjectMemos(Long projectId) {
        Memo memos = memoRepository.findByProjectId(projectId, MemoType.MAIN)
                .orElseThrow(()-> new BusinessException(ErrorCode.MEMO_NOT_FOUND));
        return MemoResponse.Converter.from(memos);
    }

    @Override
    @Transactional(readOnly = true)
    public MemoResponse getUserMemo(Long userId,Long projectId) {
        Memo memo = memoRepository.findByUserIdAndProjectId(userId,projectId, MemoType.USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_AND_USER_NOT_FOUND));

        if (memo == null) {
            return null;
        }

        return MemoResponse.Converter.from(memo);
    }

    //todo : 프로젝트 개인 메모 업데이트
    @Override
    @Transactional
    public Long updateUserMemo(Long userId, Long projectId, String content) {
        Memo memo = memoRepository.findByUserIdAndProjectId(userId, projectId,MemoType.USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMO_NOT_FOUND));

        // 더티체크 기능 활용
        memo.updateContent(content);

        Long memoId = memo.getMemoId();

        return memoId;
    }

    @Override
    @Transactional
    public Long updateProjectMemo(Long userId, Long projectId, String content) {
        System.out.println("실행시작");

        boolean hasRole = projectMemberService.hasRoleToProject(userId, projectId);

        if(hasRole){
            log.info("메모 조회 전");

            Memo memo = memoRepository.findProjectMemoByProjectId(projectId, MemoType.MAIN)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMO_NOT_FOUND));

            log.info("업데이트 전");
            memo.updateContent(content);
            log.info("업데이트 후");

            return memo.getMemoId();
        }else{
            return null;
        }

    }


}

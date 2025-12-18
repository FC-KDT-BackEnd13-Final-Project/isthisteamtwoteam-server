package org.etmetmy.bn_server.domain.memo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.dto.response.MemoResponse;
import org.etmetmy.bn_server.domain.memo.entity.MemoType;
import org.etmetmy.bn_server.domain.memo.repository.MemoRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.project.service.ProjectMemberService;

import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoServiceImpl implements MemoService{

    private final MemoRepository memoRepository;
    private final ProjectMemberService projectMemberService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;


    //프로젝트 개인 메모 생성/수정
    @Override
    @Transactional
    public Long updateUserMemo(Long userId, Long projectId, String content) {

        // content 값 체크
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);}

        // 엔티티 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 메모 객체가 존재하면 저장, 없으면 null
        Optional<Memo> memoOptional =
                memoRepository.findByUserIdAndProjectId(userId, projectId, MemoType.USER);

        // 메모 객체가 존재하지 않을 때, 객체 생성
        Memo memo = memoOptional.orElseGet(() ->
                memoRepository.save(
                        Memo.builder()
                                .project(project)
                                .user(user)
                                .memoType(MemoType.USER)
                                .build()
                )
        );
        // 더티체크로 업데이트 (신규/기존 공통)
        memo.updateContent(content);

        return memo.getMemoId();
    }

    //프로젝트 공용 메모 생성/수정
    @Override
    @Transactional
    public Long updateProjectMemo(Long userId, Long projectId, String content) {

        // content 값 체크
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);}

        // 엔티티 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 권한 체크 (관리자, 개발사만 가능)
        boolean hasRole = projectMemberService.hasRoleToProject(userId, projectId);
        if (!hasRole && user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED);
        }
        // 공용 메모는 프로젝트당 1개
        Memo memo = memoRepository
                .findProjectMemoByProjectId(projectId, MemoType.MAIN)
                .orElseGet(() ->
                        memoRepository.save(
                                Memo.builder()
                                        .project(project)
                                        .user(null)
                                        .memoType(MemoType.MAIN)
                                        .build()
                        )
                );

        memo.updateContent(content);
        return memo.getMemoId();
    }
}

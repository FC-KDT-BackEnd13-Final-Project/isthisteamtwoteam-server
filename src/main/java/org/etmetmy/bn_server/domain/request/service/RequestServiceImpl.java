package org.etmetmy.bn_server.domain.request.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestListResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ApprovalRequestResponse;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.post.repository.RequestRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.request.dto.ApprovalNotiResponse;
import org.etmetmy.bn_server.domain.request.dto.CategoryDTO;
import org.etmetmy.bn_server.domain.request.dto.PostApprovalRequestDto;
import org.etmetmy.bn_server.domain.request.dto.SummaryDTO;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.ProjectNotFoundException;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PostRepository postRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public ApprovalNotiResponse getApprovalRequest(Long projectId, Long loginUserId) {

        // 유저 검증
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);

        // 프로젝트 접근 권한 확인 (ADMIN은 모든 프로젝트 접근 가능)
        if (user.getRole() != Role.ADMIN) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId()))
                throw new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED);
        }

        // 특정 프로젝트의 Request가 있는 Post 조회
        List<Post> allPostsWithRequest = postRepository.findAllPostsWithRequestByProjectIds(List.of(projectId));
        List<PostApprovalRequestDto> allPosts = PostApprovalRequestDto.Converter.from(allPostsWithRequest);

        // 1. 상태별 count (DB)
        List<Object[]> statusCounts = requestRepository.countByStatus(List.of(projectId));
        SummaryDTO summary = SummaryDTO.Converter.from(statusCounts);

        // 2. Stage별 카테고리 나누기
        List<CategoryDTO> categories = CategoryDTO.Converter.fromAll(allPosts);

        // 3. 응답 생성
        return ApprovalNotiResponse.Converter.of(summary, categories);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalRequestListResponse getAdminApprovalRequest(Long loginUserId) {
        // 유저 검증
        userRepository.findById(loginUserId)
                .orElseThrow(UserNotFoundException::new);

        // Request가 있는 모든 Post 조회 (상태별, 단계별 카운팅 및 리스트 생성용)
        List<Post> allPostsWithRequest = postRepository.findAllPostsWithRequest();
        List<ApprovalRequestResponse> allPosts = ApprovalRequestResponse.Converter.from(allPostsWithRequest);

        // Converter 에서 단계별 필터링 및 응답 생성
        return ApprovalRequestListResponse.Converter.of(allPosts);
    }
}

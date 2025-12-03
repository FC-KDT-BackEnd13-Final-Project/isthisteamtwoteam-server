package org.etmetmy.bn_server.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.post.dto.response.PostDetailResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.post.repository.RequestRepository;
import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨 트랜잭션 정의
public class PostServiceImpl implements  PostService {

    private final PostRepository postRepository;
    private final RequestRepository requestRepository;

    private static final String STATUS_APPROVED = "승인";
    private static final String STATUS_REJECTED = "거절";
    private static final String STATUS_PENDING = "대기";
    private static final Long STAGE_APPROVED_ID = 99L;
    private static final Long STAGE_REJECTED_ID = 98L;


    // 1. 게시글 상세 조회 (GET)
    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(StatusCode.POST_NOT_FOUND));

        // DTO 변환 (StageName 처리는 DTO에서 Long stageId 기반으로 처리되어야 함)
        // DTO 호출 인자를 Post와 User로 단순화함
        return PostDetailResponse.fromEntity(post, post.getUser());
    }


    // 2. 게시글 승인
    @Transactional
    public void approvePost(Long postId, Long approvingUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(StatusCode.POST_NOT_FOUND));

        Request currentRequest = requestRepository.findByPostPostIdAndApproveStatus(postId, STATUS_PENDING)
                .orElseThrow(() -> new CustomException(StatusCode.REQUEST_PENDING_NOT_FOUND));

        // 1. Post의 Stage ID를 APPROVED (99)로 변경
        post.updateStage(STAGE_APPROVED_ID);

        // 2. Request 상태를 '승인'으로 업데이트
        currentRequest.updateStatus(approvingUserId, STATUS_APPROVED, null);
    }


    // 3. 게시글 거절
    @Transactional
    public void rejectPost(Long postId, Long rejectingUserId, String rejectReason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(StatusCode.POST_NOT_FOUND));

        Request currentRequest = requestRepository.findByPostPostIdAndApproveStatus(postId, STATUS_PENDING)
                .orElseThrow(() -> new CustomException(StatusCode.REQUEST_PENDING_NOT_FOUND));

        // 1. Post의 Stage ID를 REJECTED (98)로 변경
        post.updateStage(STAGE_REJECTED_ID);

        // 2. Request 상태를 '거절'로 업데이트
        currentRequest.updateStatus(rejectingUserId, STATUS_REJECTED, rejectReason);
    }

    @Override
    public List<PostListResponse> getPostListByProjectIdAndFilter(Long projectId, String filter) {
        // 필터에 따라 게시글 조회
        List<Post> posts = getPostsByFilter(projectId, filter);
        return posts.stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());

    }

    /**
     * 필터에 따라 게시글 조회
     */
    private List<Post> getPostsByFilter(Long projectId, String filter) {
        switch (filter.toLowerCase()) {
            case "all":
                return postRepository.findAllByProjectId(projectId);
            case "finished":
                return postRepository.findCompletedByProjectId(projectId);
            case "unfinished":
                return postRepository.findUncompletedByProjectId(projectId);
            default:
                throw new CustomException(StatusCode.INVALID_FILTER);
        }
    }

}
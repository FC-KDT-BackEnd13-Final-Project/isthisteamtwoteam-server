package org.etmetmy.bn_server.domain.post.service;

import jakarta.validation.Valid;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostUpdateRequest;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostDetailResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.dto.response.ReplyPostCreateResponse;

import java.util.List;

public interface PostService {
    PostDetailResponse getPostDetail(Long postId);

    void approvePost(Long postId, Long approvingUserId);

    void rejectPost(Long postId, Long rejectingUserId, String rejectReason);

    List<PostListResponse> getPostListByProjectIdAndFilter(Long projectId, String filter);

    PostCreateResponse createPost(Long projectId, @Valid PostCreateRequest requestDto, Long loginUserId);

    ReplyPostCreateResponse createReplyPost(Long projectId, @Valid PostCreateRequest requestDto, Long postId, Long loginUserId);

    List<PostListResponse> getPostListByStage(Long projectId, String stage);

    PostCreateResponse updatePost(Long projectId, Long postId, @Valid PostUpdateRequest requestDto, Long loginUserId);

    void completePost(Long projectId, Long postId, Long loginUserId);
}





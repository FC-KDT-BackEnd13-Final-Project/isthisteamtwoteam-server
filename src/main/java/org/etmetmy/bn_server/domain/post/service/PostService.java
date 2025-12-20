package org.etmetmy.bn_server.domain.post.service;

import jakarta.validation.Valid;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostPermanentDeleteRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostRestoreRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostUpdateRequest;
import org.etmetmy.bn_server.domain.post.dto.response.*;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectRestoreResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {
    PostDetailResponse getPostDetail(Long postId);

    void approvePost(Long postId, Long loginUserId);

    void rejectPost(Long postId, Long loginUserId, String rejectReason);

    PostListByStageResponse getPostListByProjectIdAndFilter(Long projectId, String filter);

    PostCreateResponse createPost(Long projectId, @Valid PostCreateRequest requestDto, Long loginUserId);

    //PostCreateResponse updatePost(Long projectId, Long postId, @Valid PostUpdateRequest requestDto, Long loginUserId);

    PostCreateResponse updatePost(Long postId, @Valid PostUpdateRequest requestDto, Long loginUserId);

    void completePost(Long projectId, Long postId, Long loginUserId);

    void softDeletePost(Long postId, Long userId);

    PostRestoreResponse restoreDeletedPost(Long loginUserId, PostRestoreRequest request);

    PostPermanentDeleteResponse deleteDeletedPost(Long loginUserId, @Valid PostPermanentDeleteRequest request);

    List<PostTrashResponse> getDeletedPosts(Long loginUserId, Long projectId);

    org.springframework.data.domain.Page<PostTrashResponse> getDeletedPostsWithSearch(
            Long loginUserId,
            Long projectId,
            String keyword,
            Pageable pageable);
}





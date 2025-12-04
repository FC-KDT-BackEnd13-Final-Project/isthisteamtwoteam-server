package org.etmetmy.bn_server.domain.post.service;

import org.etmetmy.bn_server.domain.post.dto.response.PostDetailResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;

import java.util.List;

public interface PostService {
    PostDetailResponse getPostDetail(Long postId);


    void approvePost(Long postId, Long approvingUserId);


    void rejectPost(Long postId, Long rejectingUserId, String rejectReason);

    List<PostListResponse> getPostListByProjectIdAndFilter(Long projectId, String filter);

    List<PostListResponse> getPostListByStage(Long projectId, String stage);
}





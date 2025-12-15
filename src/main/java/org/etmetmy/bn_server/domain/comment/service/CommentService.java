package org.etmetmy.bn_server.domain.comment.service;

import org.etmetmy.bn_server.domain.comment.dto.request.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentListResponse;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentCreateResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;

public interface CommentService {
    CommentCreateResponse createComment(Long postId, CommentCreateRequest request, String clientIp, Long userId);

    CommentListResponse getCommentsByPostId(Long postId);
}

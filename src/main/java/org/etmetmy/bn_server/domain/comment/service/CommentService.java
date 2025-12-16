package org.etmetmy.bn_server.domain.comment.service;

import jakarta.validation.Valid;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentUpdateRequest;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentListResponse;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;

public interface CommentService {
    CommentResponse createComment(Long postId, CommentCreateRequest request, String clientIp, Long userId);

    CommentListResponse getCommentsByPostId(Long postId);

    CommentResponse updateComment(Long commentId, @Valid CommentUpdateRequest request, String clientIp, Long userId);
}

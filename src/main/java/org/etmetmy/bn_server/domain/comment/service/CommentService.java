package org.etmetmy.bn_server.domain.comment.service;

import jakarta.servlet.http.HttpServletRequest;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentUpdateRequest;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentListResponse;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;

public interface CommentService {
    CommentResponse createComment(Long postId, CommentCreateRequest request, HttpServletRequest servletRequest);

    CommentResponse updateComment(Long commentId, CommentUpdateRequest request, HttpServletRequest servletRequest);

    void deleteComment(Long commentId, HttpServletRequest servletRequest);

    CommentListResponse getCommentsByPostId(Long postId);

    CommentResponse mapToCommentResponseWithReplies(Comment comment);
}

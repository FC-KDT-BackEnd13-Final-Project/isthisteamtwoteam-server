package org.etmetmy.bn_server.domain.comment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentCreateResponse;
import org.etmetmy.bn_server.domain.comment.service.CommentService;
import org.etmetmy.bn_server.global.util.IpAddressUtil;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentListResponse;

@RestController
@RequestMapping("/api/v1/users/projects/posts/{postId}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //todo: 부모 댓글, 자식 댓글 작성 API
    @PostMapping("/comment")
    public CommonResponse<CommentCreateResponse> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request,
            HttpServletRequest servletRequest)
    {
        HttpSession session = servletRequest.getSession(false);
        Long userId = SessionUtil.getLoginUserId(session);

        String clientIp = IpAddressUtil.getClientIp(servletRequest);

        CommentCreateResponse response = commentService.createComment(postId, request, clientIp, userId);
        String message = request.getParentId() == null ? "댓글 작성 성공" : "대댓글 작성 성공";
        return CommonResponse.success(message, response);
    }

    //todo: 댓글 목록 조회 기능 (주 댓글 + 대댓글 계층 구조)
    @GetMapping("/comments")
    public ResponseEntity<CommonResponse<CommentListResponse>> getComments(
            @PathVariable Long postId)
    {
        CommentListResponse response = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(CommonResponse.success("댓글 목록 조회 성공", response));
    }
}
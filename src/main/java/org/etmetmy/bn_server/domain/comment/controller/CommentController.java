package org.etmetmy.bn_server.domain.comment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.dto.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.domain.comment.dto.CommentListResponse;

@RestController
@RequestMapping("/api/v1/users/projects/{projectId}/posts/{postId}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //주 댓글 작성 기능
    @PostMapping("/comment")
    public ResponseEntity<CommonResponse<Object>> createComment(
                                                                 @PathVariable Long projectId,
                                                                 @PathVariable Long postId,
                                                                 @RequestBody @Valid CommentCreateRequest request,
                                                                 HttpServletRequest servletRequest
    ) {
        commentService.createComment(postId, request, servletRequest);

        return ResponseEntity.ok(CommonResponse.success("댓글이 성공적으로 작성 되었습니다."));
    }

    // 대댓글 작성 기능
    @PostMapping("/{commentId}/recomment")
    public ResponseEntity<CommonResponse<Object>> createReply(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @PathVariable Long commentId, // Parent Comment ID
            @RequestBody @Valid CommentCreateRequest request,
            HttpServletRequest servletRequest
    ) {

        commentService.createReply(postId, commentId, request, servletRequest);
        return ResponseEntity.ok(CommonResponse.success("대댓글이 성공적으로 작성 되었습니다."));
    }

    // 댓글 목록 조회 기능 (주 댓글 + 대댓글 계층 구조)
    @GetMapping("/comments")
    public ResponseEntity<CommonResponse<CommentListResponse>> getComments(
            @PathVariable Long postId
    ) {
        CommentListResponse response = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(CommonResponse.success("댓글 목록 조회 성공", response));
    }
}
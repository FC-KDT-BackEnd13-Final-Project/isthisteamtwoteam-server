package org.etmetmy.bn_server.domain.comment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentUpdateRequest;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;
import org.etmetmy.bn_server.domain.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentListResponse;

@RestController
@RequestMapping("/api/v1/users/projects/{projectId}/posts/{postId}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentServiceImpl;

    // 댓글 작성 기능 (일반 댓글 + 대댓글)
    // commentId2가 null이면 일반 댓글, 값이 있으면 대댓글로 처리
    @PostMapping("/comment")
    public CommonResponse<CommentResponse> createComment(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request,
            HttpServletRequest servletRequest
    ) {
        CommentResponse response = commentServiceImpl.createComment(postId, request, servletRequest);
        String message = request.getCommentId2() == null
                ? "댓글이 성공적으로 작성 되었습니다."
                : "대댓글이 성공적으로 작성 되었습니다.";
        return CommonResponse.success(message, response);
    }

    // 댓글 목록 조회 기능 (주 댓글 + 대댓글 계층 구조)
    @GetMapping("/comments")
    public ResponseEntity<CommonResponse<CommentListResponse>> getComments(
            @PathVariable Long postId
    ) {
        CommentListResponse response = commentServiceImpl.getCommentsByPostId(postId);
        return ResponseEntity.ok(CommonResponse.success("댓글 목록 조회 성공", response));
    }

    // 댓글 수정 기능
    @PatchMapping("/comment/{commentId}")
    public CommonResponse<CommentResponse> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request,
            HttpServletRequest servletRequest
    ) {
        CommentResponse response = commentServiceImpl.updateComment(commentId, request, servletRequest);
        return CommonResponse.success("댓글이 성공적으로 수정 되었습니다.", response);
    }

    // 댓글 삭제 기능 (소프트 삭제)
    @DeleteMapping("/comment/{commentId}")
    public CommonResponse<Void> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            HttpServletRequest servletRequest
    ) {
        commentServiceImpl.deleteComment(commentId, servletRequest);
        return CommonResponse.success("댓글이 성공적으로 삭제 되었습니다.", null);
    }
}
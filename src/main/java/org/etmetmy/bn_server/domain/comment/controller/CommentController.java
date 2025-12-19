package org.etmetmy.bn_server.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.activityLog.aop.ActivityLogger;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentUpdateRequest;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;
import org.etmetmy.bn_server.domain.comment.service.CommentService;
import org.etmetmy.bn_server.global.util.IpAddressUtil;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.*;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentListResponse;

@Tag(name = "Comment", description = "댓글 관리 API")
@RestController
@RequestMapping("/api/v1/users/projects/posts/")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    //todo: 부모 댓글, 자식 댓글 작성 API
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다")
    @ActivityLogger(targetType = "Comment", action = "CREATE")
    @PostMapping("{postId}/comment")
    public CommonResponse<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request,
            HttpServletRequest servletRequest)
    {
        HttpSession session = servletRequest.getSession(false);
        Long userId = SessionUtil.getLoginUserId(session);

        String clientIp = IpAddressUtil.getClientIp(servletRequest);

        CommentResponse response = commentService.createComment(postId, request, clientIp, userId);
        String message = request.getParentId() == null ? "댓글 작성 성공" : "대댓글 작성 성공";
        return CommonResponse.success(message, response);
    }

    //todo: 댓글 수정 기능
    @Operation(summary = "댓글 수정", description = "게시글에 댓글을 수정합니다")
    @ActivityLogger(targetType = "Comment", action = "UPDATE")
    @PatchMapping("/comment/{commentId}")
    public CommonResponse<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request,
            HttpServletRequest servletRequest)
    {
        HttpSession session = servletRequest.getSession(false);
        Long userId = SessionUtil.getLoginUserId(session);

        String clientIp = IpAddressUtil.getClientIp(servletRequest);

        CommentResponse response = commentService.updateComment(commentId, request, clientIp, userId);
        return CommonResponse.success("댓글 수정 성공", response);
    }

    //todo: 댓글 목록 조회 기능 (주 댓글 + 대댓글 계층 구조)
    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 계층 구조로 조회합니다")
    @GetMapping("{postId}/comments")
    public CommonResponse<CommentListResponse> getComments(@PathVariable Long postId)
    {
        CommentListResponse response = commentService.getCommentsByPostId(postId);
        return CommonResponse.success("댓글 목록 조회 성공", response);
    }

    //todo: 댓글 삭제 (soft delete)
    @Operation(summary = "댓글 삭제 (soft delete)", description = "해당 댓글과 함께 파일도 soft delete 합니다.")
    @ActivityLogger(targetType = "Comment", action = "DELETE")
    @DeleteMapping("/comment/{commentId}")
    public CommonResponse<Void> softDeleteComment(@PathVariable Long commentId, HttpServletRequest servletRequest)
    {
        HttpSession session = servletRequest.getSession(false);
        Long userId = SessionUtil.getLoginUserId(session);

        commentService.softDeleteComment(commentId,userId);
        return CommonResponse.success("댓글 삭제 성공", null);
    }
}
package org.etmetmy.bn_server.domain.post.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.ActiveFileListDTO;
import org.etmetmy.bn_server.domain.post.dto.request.PostApprovalRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostDetailResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.service.PostService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/projects")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 작성 API
     */
    @PostMapping("/{projectId}/posts")
    public CommonResponse<PostCreateResponse> createPost(
            @PathVariable Long projectId,
            @Valid @RequestBody PostCreateRequest requestDto,
            HttpSession session){

        Long loginUserId = SessionUtil.getLoginUserId(session);

        PostCreateResponse response = postService.createPost(projectId, requestDto, loginUserId);
        return CommonResponse.success("게시글 작성 성공", response);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{projectId}/posts/{postId}")
    public ResponseEntity<CommonResponse<PostDetailResponse>> getPostDetail(
            @PathVariable Long projectId,
            @PathVariable Long postId
    ) {
        PostDetailResponse response = postService.getPostDetail(postId);
        return ResponseEntity.ok(CommonResponse.success("게시글 조회 성공", response));
    }

    /**
     * 게시글 승인 API
     */
    @PatchMapping("/{postId}/approval")
    public ResponseEntity<CommonResponse<Object>> approvePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostApprovalRequest request
    ) {

        postService.approvePost(postId, request.getApproverId());
        return ResponseEntity.ok(CommonResponse.success("게시글 승인 완료"));
    }

    /**
     * 게시글 거절 API
     */
    @PatchMapping("/{postId}/reject")
    public ResponseEntity<CommonResponse<Object>> rejectPost(
            @PathVariable Long postId,
            @RequestBody @Valid PostApprovalRequest request
    ) {

        postService.rejectPost(postId, request.getApproverId(), request.getRejectReason());
        return ResponseEntity.ok(CommonResponse.success("게시글 거절 완료"));
    }


    /**
     * 게시글 조회(필터)
     * all : 전체
     * finished: 완료된 게시글
     * unfinished : 미완료된 게시글
     */
    /**
     * 게시글 목록 조회 (필터)
     */
    @GetMapping("/{projectId}/posts")
    public CommonResponse<List<PostListResponse>> getPostList(
            @PathVariable Long projectId,
            @RequestParam(name = "filter", defaultValue = "all") String filter
    ) {
        List<PostListResponse> response = postService.getPostListByProjectIdAndFilter(projectId, filter);
        return CommonResponse.success("게시글 목록 조회 성공", response);
    }


}
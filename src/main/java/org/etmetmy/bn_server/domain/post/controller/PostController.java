package org.etmetmy.bn_server.domain.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.post.dto.PostApprovalRequest;
import org.etmetmy.bn_server.domain.post.dto.PostDetailResponse;
import org.etmetmy.bn_server.domain.post.service.PostService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;

@RestController
@RequestMapping("/api/v1/users/projects")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

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
    @GetMapping("/{projectId}/posts")
    public void getPostList(@RequestParam(name = "filter") String filter){
        postService.getPostListByFilter(filter);
    }
}
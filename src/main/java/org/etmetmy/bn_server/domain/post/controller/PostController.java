package org.etmetmy.bn_server.domain.post.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.post.dto.request.PostApprovalRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostDetailResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.service.PostService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/projects")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // todo: 부모 게시글, 자식 게시글 작성 API
    @PostMapping("/{projectId}/posts")
    public CommonResponse<PostCreateResponse> createPost(
            @PathVariable Long projectId,
            @Valid @RequestBody PostCreateRequest requestDto,
            HttpSession session
    ) {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        PostCreateResponse response = postService.createPost(projectId, requestDto, loginUserId);

        return CommonResponse.success("게시글 작성 성공", response);
    }

    // todo: 게시글 상세 조회 API
    @GetMapping("/posts/{postId}")
    public CommonResponse<PostDetailResponse> getPostDetail(
            @PathVariable Long postId
    ) {
        PostDetailResponse response = postService.getPostDetail(postId);
        return CommonResponse.success("게시글 조회 성공", response);

    }

    // todo: 게시글 승인 API
    @PatchMapping("/{postId}/approval")
    public ResponseEntity<CommonResponse<Object>> approvePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostApprovalRequest request
    ) {

        postService.approvePost(postId, request.getApproverId());
        return ResponseEntity.ok(CommonResponse.success("게시글 승인 완료"));
    }


    // todo: 게시글 거절 API
    @PatchMapping("posts/{postId}/reject")
    public ResponseEntity<CommonResponse<Object>> rejectPost(
            @PathVariable Long postId,
            @RequestBody @Valid PostApprovalRequest request
    ) {

        postService.rejectPost(postId, request.getApproverId(), request.getRejectReason());
        return ResponseEntity.ok(CommonResponse.success("게시글 거절 완료"));
    }

    // todo: 관리자 및 개발사가 게시글 완료하기 버튼 API
    @PatchMapping("/{projectId}/posts/{postId}/completion")
    public void completePost(@PathVariable Long projectId,
                             @PathVariable Long postId,
                             HttpSession session) {

        Long loginUserId = SessionUtil.getLoginUserId(session);
        postService.completePost(projectId, postId, loginUserId);
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
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "stage", required = false) String stage
    ) {
        List<PostListResponse> response;

        // stage 파라미터가 명시적으로 제공된 경우
        if (stage != null) {
            response = postService.getPostListByStage(projectId, stage);
            return CommonResponse.success("게시글 단계별 조회 성공", response);
        }
        // filter 파라미터가 제공되거나 파라미터가 없는 경우
        else {
            String filterValue = filter != null ? filter : "all";
            response = postService.getPostListByProjectIdAndFilter(projectId, filterValue);
            return CommonResponse.success("게시글 목록 조회 성공", response);
        }
    }

    /*@PatchMapping("/{projectId}/posts/{postId}")
    @ActivityLogger(targetType = "Post", action = "UPDATE")
    public CommonResponse<PostCreateResponse> updatePost(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest requestDto,
            HttpSession session) {

        Long loginUserId = SessionUtil.getLoginUserId(session);

        PostCreateResponse response = postService.updatePost(
                projectId, postId, requestDto, loginUserId);
        return CommonResponse.success("게시글 수정 성공", response);
    }*/
}
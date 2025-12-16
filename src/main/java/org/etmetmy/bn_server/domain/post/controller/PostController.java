package org.etmetmy.bn_server.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.activityLog.aop.ActivityLogger;
import org.etmetmy.bn_server.domain.post.dto.request.PostApprovalRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostRestoreRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostUpdateRequest;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostDetailResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostRestoreResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostListByStageResponse;
import org.etmetmy.bn_server.domain.post.service.PostService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "게시글 관리 API")
@RestController
@RequestMapping("/api/v1/users/projects")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // todo: 부모 게시글, 자식 게시글 작성 API
    @Operation(summary = "게시글 작성", description = "부모 게시글 또는 자식 게시글을 작성합니다")
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
    @Operation(summary = "게시글 상세 조회", description = "게시글의 상세 정보를 조회합니다")
    @GetMapping("/posts/{postId}")
    public CommonResponse<PostDetailResponse> getPostDetail(
            @PathVariable Long postId
    ) {
        PostDetailResponse response = postService.getPostDetail(postId);
        return CommonResponse.success("게시글 조회 성공", response);

    }

    // todo: 게시글 승인 API
    @Operation(summary = "게시글 승인", description = "게시글을 승인합니다")
    @PatchMapping("/{postId}/approval")
    public ResponseEntity<CommonResponse<Object>> approvePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostApprovalRequest request
    ) {

        postService.approvePost(postId, request.getApproverId());
        return ResponseEntity.ok(CommonResponse.success("게시글 승인 완료"));
    }

    // todo: 게시글 거절 API
    @Operation(summary = "게시글 거절", description = "게시글을 거절합니다 (사유 포함)")
    @PatchMapping("posts/{postId}/reject")
    public ResponseEntity<CommonResponse<Object>> rejectPost(
            @PathVariable Long postId,
            @RequestBody @Valid PostApprovalRequest request
    ) {

        postService.rejectPost(postId, request.getApproverId(), request.getRejectReason());
        return ResponseEntity.ok(CommonResponse.success("게시글 거절 완료"));
    }

    // todo: 관리자 및 개발사가 게시글 완료하기 버튼 API
    @Operation(summary = "게시글 완료", description = "게시글을 완료 상태로 변경합니다")
    @PatchMapping("/{projectId}/posts/{postId}/completion")
    public void completePost(@PathVariable Long projectId,
                             @PathVariable Long postId,
                             HttpSession session) {

        Long loginUserId = SessionUtil.getLoginUserId(session);
        postService.completePost(projectId, postId, loginUserId);
    }

/*    // todo: 게시글 목록 조회 (필터) API
    // all : 전체
    // finished: 완료된 게시글
    // unfinished : 미완료된 게시글
    @Operation(summary = "게시글 목록 조회", description = "프로젝트의 게시글 목록을 조회합니다 (필터 및 단계별 조회 지원)")
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
    }*/
    // todo: 게시글 목록 조회 (필터) API
    // all : 전체
    // finished: 완료된 게시글
    // unfinished : 미완료된 게시글
    @Operation(summary = "게시글 목록 조회", description = "프로젝트의 게시글 목록을 단계별로 조회합니다 (필터 조회 지원)")
    @GetMapping("/{projectId}/posts")
    public CommonResponse<PostListByStageResponse> getPostList(
            @PathVariable Long projectId,
            @RequestParam(name = "filter", required = false) String filter
    ) {
        String filterValue = filter != null ? filter : "all";
        PostListByStageResponse response = postService.getPostListByProjectIdAndFilter(projectId, filterValue);
        return CommonResponse.success("게시글 목록 조회 성공", response);
    }

    // todo: 게시글 수정 API
    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다")
    @PatchMapping("/posts/{postId}")
    @ActivityLogger(targetType = "Post", action = "UPDATE")
    public CommonResponse<PostCreateResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest requestDto,
            HttpSession session) {

        Long loginUserId = SessionUtil.getLoginUserId(session);

        PostCreateResponse response = postService.updatePost(postId, requestDto, loginUserId);
        return CommonResponse.success("게시글 수정 성공", response);
    }

    //todo: 게시글 삭제 (soft delete)
    @Operation(summary = "게시글 삭제 (soft delete)", description = "해당 게시글과 함께 댓글, 파일도 soft delete 합니다.")
    @DeleteMapping("/posts/{postId}")
    public CommonResponse<Void> softDeletePost(@PathVariable Long postId, HttpSession session)
    {
        Long userId = SessionUtil.getLoginUserId(session);

        postService.softDeletePost(postId,userId);
        return CommonResponse.success("게시글 삭제 성공", null);
    }

    //todo: 삭제된 게시글 복원
    @Operation(summary = "게시글 복원", description = "휴지통에 있는 게시글을 복원합니다")
    @PatchMapping("/posts/restore")
    public CommonResponse<PostRestoreResponse> restoreDeletedPost(
            HttpSession session, @Valid @RequestBody PostRestoreRequest request)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        PostRestoreResponse response = postService.restoreDeletedPost(loginUserId, request);

        return CommonResponse.success("삭제된 게시글 복원 성공", response);
    }
}
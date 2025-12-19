package org.etmetmy.bn_server.domain.trash.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.activityLog.aop.ActivityLogger;
import org.etmetmy.bn_server.domain.file.dto.request.FilePermanentDeleteRequest;
import org.etmetmy.bn_server.domain.file.dto.request.FileRestoreRequest;
import org.etmetmy.bn_server.domain.file.dto.response.FilePermanentDeleteResponse;
import org.etmetmy.bn_server.domain.file.dto.response.FileRestoreResponse;
import org.etmetmy.bn_server.domain.file.dto.response.FileTrashResponse;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.post.dto.request.PostPermanentDeleteRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostRestoreRequest;
import org.etmetmy.bn_server.domain.post.dto.response.PostPermanentDeleteResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostRestoreResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostTrashResponse;
import org.etmetmy.bn_server.domain.post.service.PostService;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectHardDeleteRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectRestoreRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectHardDeleteResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectRestoreResponse;
import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Trash", description = "휴지통 페이지 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class trashController {

    private final FileService fileService;
    private final PostService postService;
    private final ProjectService projectService;

    //todo: 삭제된 파일 목록 조회 API
    @Operation(summary = "삭제된 파일 조회", description = "휴지통에 있는 파일을 조회합니다")
    @GetMapping("/users/projects/{projectId}/trash/files")
    public CommonResponse<List<FileTrashResponse>> getDeletedFiles(
            @PathVariable Long projectId,
            HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        List<FileTrashResponse> response = fileService.getDeletedFiles(loginUserId, projectId);

        return CommonResponse.success("삭제된 파일 조회 성공", response);
    }

    //todo: 삭제된 파일 복원 API
    @Operation(summary = "삭제된 파일 복원", description = "휴지통에 있는 파일을 복원합니다")
    @PatchMapping("/users/projects/{projectId}/files/restore")
    public CommonResponse<FileRestoreResponse> restoreDeletedFiles(
            @PathVariable Long projectId,
            @Valid @RequestBody FileRestoreRequest request,
            HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        FileRestoreResponse response = fileService.restoreDeletedFiles(loginUserId, request);

        return CommonResponse.success("삭제된 파일 복원 성공", response);
    }

    //todo: 삭제된 파일 영구삭제 API
    @Operation(summary = "삭제된 파일 영구삭제", description = "휴지통에 있는 파일을 DB와 S3에서 완전히 삭제합니다")
    @DeleteMapping("/users/projects/{projectId}/files/trash")
    public CommonResponse<FilePermanentDeleteResponse> deleteDeletedFiles(
            @PathVariable Long projectId,
            HttpSession session,
            @Valid @RequestBody FilePermanentDeleteRequest request)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        FilePermanentDeleteResponse response = fileService.hardDeleteFiles(loginUserId, request);

        return CommonResponse.success("삭제된 파일 영구삭제 성공", response);
    }

    //todo: 삭제된 게시글 목록 조회 API
    @Operation(summary = "삭제된 게시글 조회", description = "휴지통에 있는 게시글을 조회합니다")
    @GetMapping("/users/projects/{projectId}/trash/posts")
    public CommonResponse<List<PostTrashResponse>> getDeletedPosts(
            @PathVariable Long projectId,
            HttpSession session)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        List<PostTrashResponse> response = postService.getDeletedPosts(loginUserId, projectId);

        return CommonResponse.success("삭제된 게시글 조회 성공", response);
    }

    //todo: 삭제된 게시글 복원 API
    @Operation(summary = "게시글 복원", description = "휴지통에 있는 게시글을 복원합니다")
    @PatchMapping("/users/projects/posts/restore")
    public CommonResponse<PostRestoreResponse> restoreDeletedPost(
            HttpSession session, @Valid @RequestBody PostRestoreRequest request)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        PostRestoreResponse response = postService.restoreDeletedPost(loginUserId, request);

        return CommonResponse.success("삭제된 게시글 복원 성공", response);
    }

    // Todo: 삭제된 게시글 영구삭제 API(hard delete)
    @Operation(summary = "삭제된 게시글 영구삭제", description = "휴지통에 있는 게시글을 DB와 S3에서 완전히 삭제합니다")
    @DeleteMapping("/users/projects/posts/trash")
    public CommonResponse<PostPermanentDeleteResponse> deleteDeletedPost(
            HttpSession session,
            @Valid @RequestBody PostPermanentDeleteRequest request)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        PostPermanentDeleteResponse response = postService.deleteDeletedPost(loginUserId, request);

        return CommonResponse.success("삭제된 게시글 영구삭제 성공", response);
    }

    //todo: 삭제된 프로젝트 복원
    @Operation(summary = "프로젝트 복원", description = "휴지통에 있는 프로젝트를 복원합니다")
    @PatchMapping("/admin/projects/trash/restore")
    @ActivityLogger(action = "UPDATE", targetType = "Project")
    public CommonResponse<ProjectRestoreResponse> restoreDeletedProject(
            HttpSession session,
            @Valid @RequestBody ProjectRestoreRequest request) {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        ProjectRestoreResponse response = projectService.restoreDeletedProject(loginUserId, request);

        return CommonResponse.success("삭제된 프로젝트 복원 성공", response);
    }

    // Todo: 삭제된 프로젝트 영구삭제 (hard delete)
    @Operation(summary = "프로젝트 영구삭제", description = "휴지통에 있는 프로젝트를 영구삭합니다")
    @DeleteMapping("/admin/projects/trash")
    public CommonResponse<ProjectHardDeleteResponse> hardDeleteProject(
            HttpSession session,
            @Valid @RequestBody ProjectHardDeleteRequest request)
    {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        ProjectHardDeleteResponse response = projectService.hardDeleteProject(loginUserId, request);

        return CommonResponse.success("삭제된 프로젝트 영구삭제 성공", response);
    }
}

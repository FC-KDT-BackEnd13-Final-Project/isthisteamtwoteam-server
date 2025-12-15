package org.etmetmy.bn_server.domain.post.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.etmetmy.bn_server.domain.comment.repository.CommentRepository;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.file.dto.request.FileCreateRequest;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.link.service.LinkService;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostUpdateRequest;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostDetailResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.entity.*;
import org.etmetmy.bn_server.domain.post.repository.PostNumberCounterRepository;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.post.repository.RequestRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.domain.post.repository.StageRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨 트랜잭션 정의
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final RequestRepository requestRepository;
    private final StageRepository stageRepository;
    private final UserRepository userRepository;
    private final PostNumberCounterRepository postNumberCounterRepository;
    private final ProjectRepository projectRepository;
    private final LinkRepository linkRepository; //todo: 게시글 수정 브랜치 다시 파서 지울 예정
    private final FileRepository fileRepository; //todo: 게시글 수정 브랜치 다시 파서 지울 예정
    private final ProjectMemberRepository projectMemberRepository;

    private final LinkService linkService;
    private final FileService fileService;

    private final CommentRepository commentRepository;

    // 1. 게시글 상세 조회 (GET)
    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 해당 게시글의 모든 댓글 조회 (최상위 댓글 + 대댓글)
        List<Comment> comments = commentRepository.findAllByPostId(postId);

        Request request = requestRepository.findByPostId(postId);

        // DTO 변환 (파일, 링크, 댓글 포함)
        return PostDetailResponse.Converter.fromEntity(post, post.getUser(), comments, request);
    }

    // 2. 게시글 승인
    @Transactional
    public void approvePost(Long postId, Long approvingUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        Request currentRequest = requestRepository.findByPostPostIdAndApproveStatus(post.getPostId(), RequestStatus.STATUS_PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_PENDING_NOT_FOUND));

        // 2. Request 상태를 '승인'으로 업데이트
        currentRequest.updateStatus(approvingUserId, RequestStatus.STATUS_APPROVED, null);
    }

    // 3. 게시글 거절
    @Transactional
    public void rejectPost(Long postId, Long rejectingUserId, String rejectReason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        Request currentRequest = requestRepository.findByPostPostIdAndApproveStatus(post.getPostId(), RequestStatus.STATUS_PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_PENDING_NOT_FOUND));

        // 2. Request 상태를 '거절'로 업데이트
        currentRequest.updateStatus(rejectingUserId, RequestStatus.STATUS_REJECTED, rejectReason);
    }

    @Override
    public List<PostListResponse> getPostListByProjectIdAndFilter(Long projectId, String filter) {
        // 필터에 따라 게시글 조회
        List<Post> posts = getPostsByFilter(projectId, filter);
        return posts.stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());

    }

    @Override
    public List<PostListResponse> getPostListByStage(Long projectId, String stage) {

        // 필터에 따라 게시글 조회
        List<Post> posts = getPostsByStageFilter(projectId, stage);
        return posts.stream()
                .map(PostListResponse::from)
                .toList();
    }

    private List<Post> getPostsByStageFilter(Long projectId, String stage) {

        if (stage.equals("all")) {
            return postRepository.findAllByProjectId(projectId);
        }
        Stage stageEntity = stageRepository.findByStageName(stage)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));

        return postRepository.findByProjectIdAndStageId(projectId, stageEntity.getId());
    }

    // 필터에 따라 게시글 조회
    private List<Post> getPostsByFilter(Long projectId, String filter) {
        return switch (filter.toLowerCase()) {
            case "all" -> postRepository.findAllByProjectId(projectId);
            case "finished" -> postRepository.findCompletedByProjectId(projectId);
            case "unfinished" -> postRepository.findUncompletedByProjectId(projectId);
            default -> throw new BusinessException(ErrorCode.BOARD_INVALID_FILTER);
        };
    }

    // 게시글 작성 (게시글 저장 → 링크 저장 -> 임시 파일 연결 (post_id, isTemp=false))
    @Override
    @Transactional
    public PostCreateResponse createPost(Long projectId, PostCreateRequest requestDto, Long loginUserId) {

        // 필요한 엔티티 조회
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        Stage stage = stageRepository.findById(requestDto.getStageId()).orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));

        Long postNumber = generatePostNumber(projectId);

        Post parent = null;
        if (requestDto.getParentId() != null) {
            parent = postRepository.findById(requestDto.getParentId()).orElseThrow(BoardNotFoundException::new);}

        // 1. 게시글 저장: DTO를 엔티티로 변환 후 DB에 저장, ID 발급
        Post savedPost = postRepository.save(
                PostCreateRequest.Converter.toEntity(project, user, stage, postNumber, parent, requestDto)
        );

        // 2. 승인요청이 있는 경우에만 Request 엔티티 생성 (초기 상태: PENDING)
        Request request = PostCreateRequest.Converter.toRequestEntity(requestDto, savedPost, loginUserId);
        if (request != null) {
            requestRepository.save(request);
        }

        // 3. 링크 저장: 전달받은 링크 URL 리스트를 Link 엔티티로 변환 후 게시글과 연동
        linkService.saveLinks(savedPost, requestDto.getLinkUrls(), loginUserId);

        // 4. 임시 파일 연결: 프론트에서 전달받은 fileIds를 기준으로 DB 에서 임시 파일(isTemp=true)을 조회
        fileService.saveFiles(savedPost, requestDto.getFileIds(), loginUserId);

        // 5. 저장된 데이터 재조회
        List<File> files = fileRepository.findFilesByPostId(savedPost.getPostId());
        List<Link> links = linkRepository.findLinksByPostId(savedPost.getPostId());

        return PostCreateResponse.Converter.from(savedPost, FileInfoDTO.Converter.from(files), LinkInfoDTO.Converter.from(links));
    }

    // 게시글 수정
    @Override
    @Transactional
    public PostCreateResponse updatePost(Long projectId, Long postId, @Valid PostUpdateRequest requestDto, Long loginUserId) {

        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 2. 게시글이 해당 프로젝트에 속하는지 검증
        if (!post.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.POST_PROJECT_MISMATCH);
        }

        // 3. 작성자 권한 검증 (작성자만 수정 가능)
        if(!post.getUser().getId().equals(loginUserId)){
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }

        // 4. Stage 조회
        Stage stage = null;
        if (requestDto.getStageId() != null) {
            stage = stageRepository.findById(requestDto.getStageId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
        }

        // 5. 기본 필드 업데이트 (title, content, stage)
        PostUpdateRequest.Converter.applyTo(requestDto, post, stage);

        // 6. 파일 삭제 처리 (removeFileIds가 제공된 경우)
        if (requestDto.getRemoveFileIds() != null && !requestDto.getRemoveFileIds().isEmpty()) {
            List<File> filesToDelete = fileRepository.findAllById(requestDto.getRemoveFileIds());

            // 파일이 해당 게시글에 속하는지 검증
            for (File file : filesToDelete) {
                if (file.getPost() == null || !file.getPost().getPostId().equals(postId)) {
                    throw new BusinessException(ErrorCode.FILE_NOT_IN_POST);
                }
            }

            // Soft Delete 적용
            for (File file : filesToDelete) {
                file.softDelete(loginUserId);
            }
        }

        // 7. 파일 추가 처리 (addFileIds가 제공된 경우)
        if (requestDto.getAddFileIds() != null && !requestDto.getAddFileIds().isEmpty()) {
            fileService.saveFiles(post, requestDto.getAddFileIds(), loginUserId);
        }

        // 8. 링크 업데이트 (링크 URL이 제공된 경우)
        if (requestDto.getLinkUrls() != null) {
            // 기존 링크 조회 및 삭제
            List<Link> existingLinks = linkRepository.findByPost(post);
            linkRepository.deleteAll(existingLinks);

            // 새 링크 저장
            linkService.saveLinks(post, requestDto.getLinkUrls(), loginUserId);
        }

        // 9. 승인요청 상태 업데이트 (requestApproval 필드가 제공된 경우)
        if (requestDto.getRequestApproval() != null) {
            // 기존 PENDING 상태의 Request 조회
            Request existingRequest = requestRepository.findByPostPostIdAndApproveStatus(
                    postId, RequestStatus.STATUS_PENDING).orElse(null);

            if (Boolean.TRUE.equals(requestDto.getRequestApproval())) {
                // 승인요청을 원하는 경우
                if (existingRequest == null) {
                    // 기존 승인요청이 없으면 새로 생성
                    Request newRequest = PostUpdateRequest.Converter.toRequestEntity(requestDto, post, loginUserId);
                    requestRepository.save(Objects.requireNonNull(newRequest));
                }
                // 이미 PENDING 상태의 승인요청이 있으면 그대로 유지
            } else {
                // 승인요청을 취소하는 경우
                if (existingRequest != null) {
                    // 기존 PENDING 승인요청이 있으면 삭제
                    requestRepository.delete(existingRequest);
                }
            }
        }

        // 10. 저장된 데이터 재조회
        List<File> files = fileRepository.findFilesByPostId(post.getPostId());
        List<Link> links = linkRepository.findLinksByPostId(post.getPostId());

        return PostCreateResponse.Converter.from(
                post, FileInfoDTO.Converter.from(files), LinkInfoDTO.Converter.from(links));
    }

    @Override
    @Transactional
    public void completePost(Long projectId, Long postId, Long loginUserId) {
        User user = userRepository.findById(loginUserId)
                .orElseThrow(UserNotFoundException::new);

        // ADMIN과 DEVELOPER 권한 확인 (개발사만 완료 처리 가능)
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.DEVELOPER) {
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }

        // ADMIN이 아닌 경우에만 프로젝트 멤버 검증
        if (user.getRole() != Role.ADMIN) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, loginUserId)) {
                throw new BusinessException(ErrorCode.PROJECT_AND_USER_NOT_FOUND);
            }
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 게시글이 해당 프로젝트에 속하는지 검증
        if (!post.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.POST_PROJECT_MISMATCH);
        }

        // 완료 상태로 업데이트
        post.updateCompletedStatus(true);
    }

    // 프로젝트–게시글 소속 검증
    public static void validatePostBelongsToProject(Post post, Project project) {
        if (!post.getProject().getId().equals(project.getId())) {
            throw new BusinessException(ErrorCode.POST_PROJECT_MISMATCH);
        }
    }

    // 작성자 검증
    public static void validateWriter(Post post, Long loginUserId) {
        if (!post.getUser().getId().equals(loginUserId)) {
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }
    }

    // Counter Table로 프로젝트 내 게시글 번호 생성 (낙관적 락 적용)
    private Long generatePostNumber(Long projectId) {
        PostNumberCounter counter = postNumberCounterRepository.findByProjectId(projectId)
                .orElseGet(() -> {
                    PostNumberCounter newCounter = PostNumberCounter.builder()
                            .projectId(projectId)
                            .currentNumber(0L)
                            .build();

                    // DB에 먼저 저장
                    return postNumberCounterRepository.save(newCounter);
                });

        Long postNumber = counter.getNextNumber();

        // version 증가시키려면 save 반드시 필요
        postNumberCounterRepository.save(counter);

        return postNumber;
    }
}
package org.etmetmy.bn_server.domain.post.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.etmetmy.bn_server.domain.comment.repository.CommentRepository;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.file.dto.response.FileListResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.file.dto.response.FileTrashResponse;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.link.service.LinkService;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostPermanentDeleteRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostRestoreRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostUpdateRequest;
import org.etmetmy.bn_server.domain.post.dto.response.*;
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
import org.etmetmy.bn_server.domain.history.entity.ChangeType;
import org.etmetmy.bn_server.domain.history.event.HistoryFileEvent;
import org.etmetmy.bn_server.domain.history.event.HistoryLinkEvent;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.*;
import org.etmetmy.bn_server.global.util.IpAddressUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
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

    private final ApplicationEventPublisher eventPublisher;

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
    public PostListByStageResponse getPostListByProjectIdAndFilter(Long projectId, String filter) {
        // 필터에 따라 게시글 조회 (전체 리스트)
        List<Post> posts = getPostsByFilter(projectId, filter);

        List<Long> postIds = posts.stream()
                .map(Post::getPostId)
                .toList();

        // 3. Request 일괄 조회 (N+1 문제 방지)
        List<Request> requests = requestRepository.findByPostPostIdIn(postIds);

        // 4. Post ID를 키로 하는 Request Map 생성
        Map<Long, Request> requestMap = requests.stream()
                .collect(Collectors.toMap(
                        r -> r.getPost().getPostId(),
                        r -> r,
                        (existing, replacement) -> existing // 중복 시 첫 번째 값 유지
                ));

        // Post를 PostListResponse로 변환
        List<PostListResponse> postListResponses = posts.stream()
                .map(post -> PostListResponse.from(post, requestMap.get(post.getPostId())))
                .toList();

        // 프로젝트의 모든 파일 조회 (project_id로 직접 조회)
        List<File> files = fileRepository.findAllByProjectId(projectId);
        List<FileListResponse> fileListResponses = files.stream()
                .map(FileListResponse::from)
                .toList();

        // 단계별로 그룹핑하여 반환
        return PostListByStageResponse.Converter.of(postListResponses, fileListResponses);
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
        Stage stage = stageRepository.findByStageName(requestDto.getStageName()).orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));

        Long postNumber = generatePostNumber(projectId);

        Post parent = null;
        if (requestDto.getParentId() != null) {
            parent = postRepository.findById(requestDto.getParentId()).orElseThrow(BoardNotFoundException::new);}

        // 1. 게시글 저장: DTO를 엔티티로 변환 후 DB에 저장, ID 발급
        Post post = PostCreateRequest.Converter.toEntity(project, user, stage, postNumber, parent, requestDto);
        Post savedPost = postRepository.save(post);

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
    public PostCreateResponse updatePost(Long postId, @Valid PostUpdateRequest requestDto, Long loginUserId) {

        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 2. 작성자 권한 검증 (작성자만 수정 가능)
        if(!post.getUser().getId().equals(loginUserId)){
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }

        // 3. Stage 조회
        Stage stage = null;
        if (requestDto.getStage() != null) {
            stage = stageRepository.findByStageName(requestDto.getStage())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
        }

        // 4. 기본 필드 업데이트 (title, content, stage)
        PostUpdateRequest.Converter.applyTo(requestDto, post, stage);

        // 5. 파일 삭제 처리 (removeFileIds가 제공된 경우)
        if (requestDto.getRemoveFileIds() != null && !requestDto.getRemoveFileIds().isEmpty()) {
            List<File> filesToDelete = fileRepository.findAllById(requestDto.getRemoveFileIds());

            // 파일이 해당 게시글에 속하는지 검증
            for (File file : filesToDelete) {
                if (file.getPost() == null || !file.getPost().getPostId().equals(postId)) {
                    throw new BusinessException(ErrorCode.FILE_NOT_IN_POST);
                }
            }

            // IP 주소 가져오기
            String clientIp = null;
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    clientIp = IpAddressUtil.getClientIp(request);
                }
            } catch (Exception e) {
                // RequestContext가 없는 경우 null로 저장
            }

            // Soft Delete 적용 + 히스토리 이벤트 발행
            for (File file : filesToDelete) {
                file.softDelete(loginUserId);

                // 파일 히스토리 이벤트 발행 (DELETE)
                eventPublisher.publishEvent(
                        new HistoryFileEvent(file, ChangeType.DELETE, loginUserId, clientIp)
                );
            }
        }

        // 6. 파일 추가 처리 (addFileIds가 제공된 경우)
        if (requestDto.getAddFileIds() != null && !requestDto.getAddFileIds().isEmpty()) {
            fileService.saveFiles(post, requestDto.getAddFileIds(), loginUserId);
        }

        // 7. 링크 업데이트 (링크 URL이 제공된 경우)
        if (requestDto.getLinkUrls() != null) {
            // 기존 링크 조회
            List<Link> existingLinks = linkRepository.findByPost(post);

            // IP 주소 가져오기
            String clientIp = null;
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    clientIp = IpAddressUtil.getClientIp(request);
                }
            } catch (Exception e) {
                // RequestContext가 없는 경우 null로 저장
            }

            // 기존 링크 삭제 + 히스토리 이벤트 발행
            for (Link link : existingLinks) {
                // 링크 히스토리 이벤트 발행 (DELETE)
                eventPublisher.publishEvent(
                        new HistoryLinkEvent(link, ChangeType.DELETE, loginUserId, clientIp)
                );
            }
            linkRepository.deleteAll(existingLinks);

            // 새 링크 저장
            linkService.saveLinks(post, requestDto.getLinkUrls(), loginUserId);
        }

        // 8. 승인요청 상태 업데이트 (requestApproval 필드가 제공된 경우)
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

        // 9. 저장된 데이터 재조회
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

        // ADMIN과 DEVELOPER 권한 확인 (관리자와 개발사 처리 가능)
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

    // 게시글 삭제 (soft delete)
    @Override
    @Transactional
    public void softDeletePost(Long postId, Long userId){

        // 1. 엔티티 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_POST_NOT_FOUND));
        List<Comment>  comments = commentRepository.findAllByPostId(post.getPostId());

        // 2. 작성자 권한 검증 (작성자만 삭제 가능)
        if(!post.getUser().getId().equals(userId)){
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }

        // 3. 이미 삭제된 게시글 확인
        if (post.getIsDeleted()) {
            throw new BusinessException(ErrorCode.BOARD_ALREADY_DELETED);
        }

        // 4. 게시글 soft delete
        post.softDelete(userId);

        // 5. 게시글과 연결된 파일 삭제
        List<File> postFiles = fileRepository.findFilesByPostId(post.getPostId());
        for (File fileToDelete : postFiles) {
            fileToDelete.softDelete(userId);
        }
    }

    // 게시글 복원
    @Override
    @Transactional
    public PostRestoreResponse restoreDeletedPost(Long loginUserId, PostRestoreRequest request){

        // 1. 권한 검증 (관리자만)
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);
        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.DELETED_BOARD_ACCESS_DENIED);
        }

        // 2. 요청한 게시글 ID 조회
        List<Long> postIds = request.getPostIds();
        List<Post> posts = postRepository.findAllById(postIds);

        // 3. 존재 개수 비교
        if (posts.size() != postIds.size()) {
            throw new BoardNotFoundException("존재하지 않는 게시글이 포함되어 있습니다.");
        }

        // 4. 삭제 여부 체크
        posts.forEach(post -> {
            if (!post.getIsDeleted()) {
                throw new BusinessException(ErrorCode.BOARD_NOT_DELETED);
            }
        });

        // 5. 모든 게시글의 파일을 한 번에 조회 (N+1 문제 해결)
        List<File> allFiles = fileRepository.findByPostIds(postIds);
        Map<Long, List<File>> filesByPostId = allFiles.stream()
                .collect(Collectors.groupingBy(file -> file.getPost().getPostId()));

        // 6. 게시글과 파일 복원
        posts.forEach(post -> {
            post.restore();

            // 게시글에 속한 파일 복원
            List<File> postFiles = filesByPostId.getOrDefault(post.getPostId(), List.of());
            postFiles.forEach(File::restore);
        });

        postRepository.saveAll(posts);
        return PostRestoreResponse.Converter.from(posts, user.getId());
    }

    // 게시글 영구삭제 (hard delete)
    @Override
    @Transactional
    public PostPermanentDeleteResponse deleteDeletedPost(Long loginUserId, @Valid PostPermanentDeleteRequest request){

        // 1. 삭제 요청한 게시글 ID 목록 조회
        List<Long> postIds = request.getPostIds();
        List<Post> posts = postRepository.findAllById(postIds);

        // 2. 존재 개수 비교
        if (posts.size() != postIds.size()) {
            throw new BoardNotFoundException("존재하지 않는 게시글이 포함되어 있습니다.");
        }

        // 3. 삭제 여부 체크
        posts.forEach(post -> {
            if (!post.getIsDeleted()) {
                throw new BusinessException(ErrorCode.BOARD_NOT_DELETED);}
        });

        // 4. S3 파일 삭제
        List<File> files = fileRepository.findByPostIds(postIds);
        fileService.deleteFilesFromS3(files);

        // 5. 게시글 삭제 (Cascade로 연관 엔티티도 삭제)
        postRepository.deleteAll(posts);

        return PostPermanentDeleteResponse.Converter.from(posts);
    }

    // 삭제된 프로젝트 조회
    @Override
    @Transactional
    public List<PostTrashResponse> getDeletedPosts(Long loginUserId, Long projectId){

        // 권한 검증 (관리자와 담당 개발사만 접근가능)
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);
        boolean hasRole = projectMemberRepository.existsByProjectIdAndUserId(projectId, loginUserId);

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isDeveloperInProject = hasRole && user.getRole() == Role.DEVELOPER;

        if (!isAdmin && !isDeveloperInProject)
            throw new BusinessException(ErrorCode.DELETED_FILE_ACCESS_DENIED);

        List<Post> deletedPosts = postRepository.findDeletedPostsByProjectId(projectId);
        return PostTrashResponse.Converter.from(deletedPosts);

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

package org.etmetmy.bn_server.domain.post.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.request.FileCreateRequest;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.link.dto.LinkCreateRequest;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.dto.request.PostUpdateRequest;
import org.etmetmy.bn_server.domain.post.dto.request.RequestCreateRequest;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostDetailResponse;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.dto.response.ReplyPostCreateResponse;
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

import java.util.ArrayList;
import java.util.List;
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
    private final ProjectMemberRepository projectMemberRepository;
    private final FileRepository fileRepository;
    private final LinkRepository linkRepository;

    // 1. 게시글 상세 조회 (GET)
    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // DTO 변환 (StageName 처리는 DTO 에서 Long stageId 기반으로 처리되어야 함)
        // DTO 호출 인자를 Post와 User로 단순화함
        return PostDetailResponse.Converter.fromEntity(post, post.getUser());
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

    // 게시글 작성
    @Override
    @Transactional
    public PostCreateResponse createPost(Long projectId, PostCreateRequest requestDto, Long loginUserId) {

        // 필요한 엔티티 조회
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        Stage stage = stageRepository.findById(requestDto.getStageId()).orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));

        // Counter Table로 프로젝트 내 게시글 번호 생성 (낙관적 락 적용)
        PostNumberCounter counter = postNumberCounterRepository.findByProjectId(projectId)
                .orElseGet(() -> PostNumberCounter.builder()
                        .projectId(projectId)
                        .currentNumber(0L)
                        .build());
        Long postNumber = counter.getNextNumber();
        postNumberCounterRepository.save(counter);

        // Post 엔티티 생성 및 저장
        Post post = PostCreateRequest.Converter.toEntity(project, user, requestDto.getTitle(), requestDto.getContent(), stage, postNumber);
        Post savedPost = postRepository.save(post);

        // Request 엔티티 생성 및 저장
        requestRepository.save(RequestCreateRequest.Converter.toEntity(savedPost, loginUserId));

        // 파일 처리
        List<File> savedFiles = new ArrayList<>();
        if (requestDto.getFileInfos() != null && !requestDto.getFileInfos().isEmpty()) {
            for (PostCreateRequest.FileInfo fileInfo : requestDto.getFileInfos()) {
                File file = FileCreateRequest.Converter.toEntity(
                        fileInfo.getFileUrl(),
                        fileInfo.getFileSize(),
                        savedPost,
                        loginUserId
                );
                savedFiles.add(file);
            }
            fileRepository.saveAll(savedFiles);
        }

        // 링크 처리
        List<Link> savedLinks = new ArrayList<>();
        if (requestDto.getLinkUrls() != null && !requestDto.getLinkUrls().isEmpty()) {
            for (String linkUrl : requestDto.getLinkUrls()) {
                Link link = LinkCreateRequest.Converter.toEntity(linkUrl, savedPost, loginUserId);
                savedLinks.add(link);
            }
            linkRepository.saveAll(savedLinks);
        }

        // 응답 반환
        return PostCreateResponse.Converter.from(savedPost, savedFiles, savedLinks);
    }

    @Override
    @Transactional
    public ReplyPostCreateResponse createReplyPost(Long projectId, PostCreateRequest requestDto, Long postId, Long loginUserId) {

        // 1. 필요한 엔티티 조회
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        Stage stage = stageRepository.findById(requestDto.getStageId()).orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(BoardNotFoundException::new);

        // 2. Counter Table로 프로젝트 내 게시글 번호 생성 (낙관적 락 적용)
        PostNumberCounter counter = postNumberCounterRepository.findByProjectId(projectId)
                .orElseGet(() -> PostNumberCounter.builder()
                        .projectId(projectId)
                        .currentNumber(0L)
                        .build());
        Long postNumber = counter.getNextNumber();
        postNumberCounterRepository.save(counter);

        // 3. Post 엔티티 생성 및 저장
        Post replyPost = PostCreateRequest.Converter.toReplyEntity(project, user,post, requestDto.getTitle(), requestDto.getContent(), stage, postNumber);
        Post savedPost = postRepository.save(replyPost);

        // Request 엔티티 생성 및 저장
        requestRepository.save(RequestCreateRequest.Converter.toEntity(savedPost, loginUserId));

        // 4. 파일 처리
        List<File> savedFiles = new ArrayList<>();
        if (requestDto.getFileInfos() != null && !requestDto.getFileInfos().isEmpty()) {
            for (PostCreateRequest.FileInfo fileInfo : requestDto.getFileInfos()) {
                File file = FileCreateRequest.Converter.toEntity(
                        fileInfo.getFileUrl(),
                        fileInfo.getFileSize(),
                        savedPost,
                        loginUserId
                );
                savedFiles.add(file);
            }
            fileRepository.saveAll(savedFiles);
        }

        // 5. 링크 처리

        List<Link> savedLinks = new ArrayList<>();
        if (requestDto.getLinkUrls() != null && !requestDto.getLinkUrls().isEmpty()) {
            for (String linkUrl : requestDto.getLinkUrls()) {
                Link link = LinkCreateRequest.Converter.toEntity(linkUrl, savedPost, loginUserId);
                savedLinks.add(link);
            }
            linkRepository.saveAll(savedLinks);
        }

        // 6. 응답 반환
        return ReplyPostCreateResponse.Converter.from(savedPost, post, savedFiles, savedLinks);
    }

    // 게시글 업데이트 API
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

        // 3. 작성자 권한 검증
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

        // 6. 링크 업데이트 (링크 URL이 제공된 경우)
        if (requestDto.getLinkUrls() != null) {
            // 기존 링크 조회
            List<Link> existingLinks = linkRepository.findByPost(post);

            // 기존 링크 삭제 (물리적 삭제 - orphanRemoval로 자동 처리됨)
            linkRepository.deleteAll(existingLinks);

            // 새 링크 추가
            List<Link> newLinks = new ArrayList<>();
            for (String linkUrl : requestDto.getLinkUrls()) {
                Link link = LinkCreateRequest.Converter.toEntity(linkUrl, post, loginUserId);
                newLinks.add(link);
            }
            linkRepository.saveAll(newLinks);
        }

        // 7. TODO: 파일 업데이트 로직은 별도 API로 구현 필요

        // 8. 응답 반환 (파일, 링크 정보 포함)
        List<File> savedFiles = fileRepository.findByPost(post);
        List<Link> savedLinks = linkRepository.findByPost(post);
        return PostCreateResponse.Converter.from(post, savedFiles, savedLinks);
    }

    @Override
    @Transactional
    public void completePost(Long projectId, Long postId, Long loginUserId) {
        User user = userRepository.findById(loginUserId)
                .orElseThrow(UserNotFoundException::new);

        // 프로젝트 멤버 검증 (로그인 유저가 해당 프로젝트의 멤버인지 확인)
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, loginUserId)) {
            throw new BusinessException(ErrorCode.PROJECT_AND_USER_NOT_FOUND);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 게시글이 해당 프로젝트에 속하는지 검증
        if (!post.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.POST_PROJECT_MISMATCH);
        }

        // ADMIN과 DEVELOPER 권한 확인 (개발사만 완료 처리 가능)
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.DEVELOPER) {
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
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
}
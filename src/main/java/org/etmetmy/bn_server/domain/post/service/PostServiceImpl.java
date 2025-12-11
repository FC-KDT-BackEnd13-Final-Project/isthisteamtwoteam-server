package org.etmetmy.bn_server.domain.post.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.link.dto.LinkCreateRequest;
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
    private final LinkRepository linkRepository; //todo: 게시글 수정 브랜치 다시 파서 지울 예정
    private final FileRepository fileRepository; //todo: 게시글 수정 브랜치 다시 파서 지울 예정
    private final ProjectMemberRepository projectMemberRepository;
    private final LinkService linkService;
    private final FileService fileService;


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

        Long postNumber = generatePostNumber(projectId);

        Post parent = null;
        if (requestDto.getParentId() != null) {
            parent = postRepository.findById(requestDto.getParentId())
                    .orElseThrow(BoardNotFoundException::new);
        }
        Post savedPost = postRepository.save(
                PostCreateRequest.Converter.toEntity(project, user, stage, postNumber, parent, requestDto)
        );

        linkService.saveLinks(savedPost, requestDto.getLinkUrls(), loginUserId);
        fileService.saveFiles(savedPost, requestDto.getFileInfos(), loginUserId);

        // files, links를 각각 fetch (MultipleBagFetchException 방지)
        // 첫 번째 쿼리: files 초기화
        postRepository.findByIdWithFiles(savedPost.getPostId())
                .orElseThrow(BoardNotFoundException::new);

        // 두 번째 쿼리: links 초기화 (같은 영속성 컨텍스트, files와 links 모두 초기화됨)
        Post postWithFilesAndLinks = postRepository.findByIdWithLinks(savedPost.getPostId())
                .orElseThrow(BoardNotFoundException::new);

        return PostCreateResponse.Converter.from(postWithFilesAndLinks);
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


            List<Link> newLinks = new ArrayList<>();
            for (String linkUrl : requestDto.getLinkUrls()) {
                Link link = LinkCreateRequest.Converter.toEntity(post, linkUrl, loginUserId);
                newLinks.add(link);
            }
            linkRepository.saveAll(newLinks);
        }

        // 7. TODO: 파일 업데이트 로직은 별도 API로 구현 필요

        // 8. 응답 반환 (파일, 링크 정보 포함)
        List<File> savedFiles = fileRepository.findByPost(post);
        List<Link> savedLinks = linkRepository.findByPost(post);
       // todo: 수정예정 return PostCreateResponse.Converter.from(post, savedFiles, savedLinks);
        return PostCreateResponse.Converter.from(post);
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
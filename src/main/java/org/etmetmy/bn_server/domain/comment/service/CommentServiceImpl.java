package org.etmetmy.bn_server.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentListResponse;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentCreateResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.comment.repository.CommentRepository;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.link.service.LinkService;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BoardNotFoundException;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨에서 트랜잭션 관리
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final LinkRepository linkRepository;
    private final FileService fileService;
    private final LinkService linkService;


    // 댓글 생성 (일반 댓글 + 대댓글 통합)
    public CommentCreateResponse createComment(
            Long postId, CommentCreateRequest request, String clientIp, Long userId)
    {
        // 1. 필요한 엔티티 조회
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Post post = postRepository.findById(postId).orElseThrow(BoardNotFoundException::new);

        // 2. 부모 댓글 조회 (대댓글인 경우)
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARENT_COMMENT_NOT_FOUND));

            // 부모 댓글이 현재 게시글에 속하는지 검증
            if (!parent.getPost().getPostId().equals(postId)) {
                throw new BusinessException(ErrorCode.INVALID_PARENT_COMMENT);
            }
        }

        // 3. 댓글 엔티티 생성 & 저장
        Comment comment = CommentCreateRequest.Converter.toEntity(post, request, user, parent, clientIp);
        Comment savedComment = commentRepository.save(comment);

        // 4. 링크 저장: 전달받은 링크 URL 리스트를 Link 엔티티로 변환 후 게시글과 연동
        linkService.saveLinks(savedComment, request.getLinkUrls(), userId);

        // 5. 임시 파일 연결: 프론트에서 전달받은 fileIds를 기준으로 DB 에서 임시 파일(isTemp=true)을 조회
        fileService.saveFiles(savedComment, request.getFileIds(), userId);

        // 6. 저장된 데이터 재조회
        List<File> files = fileRepository.findFilesByPostId(savedComment.getCommentId());
        List<Link> links = linkRepository.findLinksByPostId(savedComment.getCommentId());

        return CommentCreateResponse.Converter.from(
                savedComment, FileInfoDTO.Converter.from(files), LinkInfoDTO.Converter.from(links), List.of());
    }

    // 게시글 ID에 해당하는 모든 댓글 목록을 계층 구조로 조회
    @Transactional(readOnly = true)
    public CommentListResponse getCommentsByPostId(Long postId) {

        // 1. Post 존재 여부 확인 (외래 키 검증)
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 2. 게시글의 모든 댓글 한 번에 조회
        List<Comment> comments = commentRepository.findAllByPostId(postId);

        // 3. 댓글 ID 기준 파일/링크 맵 구성
        List<File> files = fileRepository.findByPost_PostIdAndCommentIsNotNull(postId);

        Map<Long, List<FileInfoDTO>> fileMap = files.stream()
                .filter(f -> f.getComment() != null && !f.getIsDeleted()) // null, 삭제 파일 제외
                .collect(Collectors.groupingBy(
                        f -> f.getComment().getCommentId(),
                        Collectors.mapping(
                                f -> FileInfoDTO.Converter.from(List.of(f)).get(0), // 단일 객체를 리스트로 감싸 기존 Converter 호출
                                Collectors.toList()
                        )
                ));

        Map<Long, List<LinkInfoDTO>> linkMap = linkRepository.findByPostId(postId).stream()
                .filter(l -> l.getComment() != null) // 댓글 없는 링크 제외
                .collect(Collectors.groupingBy(
                        l -> l.getComment().getCommentId(),
                        Collectors.mapping(l -> LinkInfoDTO.Converter.from(List.of(l)).get(0),
                                Collectors.toList())
                ));

        // 4. parentId 기준으로 댓글 그룹핑
        Map<Long, List<Comment>> childrenMap =
                comments.stream()
                        .filter(comment -> comment.getParent() != null)
                        .collect(Collectors.groupingBy(
                                comment -> comment.getParent().getCommentId()));

        // 5. 최상위 댓글만 트리 구성
        List<CommentCreateResponse> responses =
                comments.stream()
                        .filter(comment -> comment.getParent() == null)
                        .map(comment ->
                                buildCommentTree(
                                        comment, childrenMap, fileMap, linkMap)
                        )
                        .toList();

        return CommentListResponse.Converter.from(post, comments, responses);
    }

    // 댓글 트리 생성
    private CommentCreateResponse buildCommentTree(
            Comment comment,
            Map<Long, List<Comment>> childrenMap,
            Map<Long, List<FileInfoDTO>> fileMap,
            Map<Long, List<LinkInfoDTO>> linkMap
    ) {
        // 하위 댓글 재귀 처리 (메모리)
        List<CommentCreateResponse> replies =
                childrenMap
                        .getOrDefault(comment.getCommentId(), List.of())
                        .stream()
                        .map(child ->
                                buildCommentTree(child, childrenMap, fileMap, linkMap)
                        )
                        .toList();

        return CommentCreateResponse.Converter.from(
                comment,
                fileMap.getOrDefault(comment.getCommentId(), List.of()),
                linkMap.getOrDefault(comment.getCommentId(), List.of()),
                replies
        );
    }
}
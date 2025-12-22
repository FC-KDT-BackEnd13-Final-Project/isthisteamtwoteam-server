package org.etmetmy.bn_server.domain.comment.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentUpdateRequest;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentListResponse;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;
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
import org.etmetmy.bn_server.domain.post.dto.request.PostUpdateRequest;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
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
    public CommentResponse createComment(
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
        List<File> files = fileRepository.findFilesByCommentId(savedComment.getCommentId());
        List<Link> links = linkRepository.findLinksByCommentId(savedComment.getCommentId());

        return CommentResponse.Converter.from(
                savedComment, FileInfoDTO.Converter.from(files), LinkInfoDTO.Converter.from(links), List.of(), userId);
    }

    // 게시글 ID에 해당하는 모든 댓글 목록을 계층 구조로 조회
    @Transactional(readOnly = true)
    public CommentListResponse getCommentsByPostId(Long postId, Long loginUserId) {

        // 1. Post 존재 여부 확인 (외래 키 검증)
        Post post = postRepository.findById(postId).orElseThrow(BoardNotFoundException::new);

        // 2. 게시글의 모든 댓글 한 번에 조회
        List<Comment> comments = commentRepository.findAllByPostId(postId);

        // 3. 댓글 ID 기준 파일/링크 맵 구성
        Map<Long, List<FileInfoDTO>> fileMap = fileRepository.findByPostId(postId).stream()
                .filter(f -> f.getComment() != null && !f.getIsDeleted()) // null, 삭제 파일 제외
                .collect(Collectors.groupingBy(
                        f -> f.getComment().getCommentId(),
                        Collectors.mapping(
                                f -> FileInfoDTO.Converter.from(List.of(f)).getFirst(),
                                Collectors.toList()
                        )
                ));

        Map<Long, List<LinkInfoDTO>> linkMap = linkRepository.findByPostId(postId).stream()
                .filter(l -> l.getComment() != null) // 댓글 없는 링크 제외
                .collect(Collectors.groupingBy(
                        l -> l.getComment().getCommentId(),
                        Collectors.mapping(l -> LinkInfoDTO.Converter.from(List.of(l)).getFirst(),
                                Collectors.toList())
                ));

        // 4. parentId 기준으로 댓글 그룹핑
        Map<Long, List<Comment>> childrenMap =
                comments.stream()
                        .filter(comment -> comment.getParent() != null)
                        .collect(Collectors.groupingBy(
                                comment -> comment.getParent().getCommentId()));

        // 5. 최상위 댓글만 트리 구성
        List<CommentResponse> responses = comments.stream()
                .filter(comment -> comment.getParent() == null)
                .map(comment -> buildCommentTree(comment, childrenMap, fileMap, linkMap, loginUserId)) // ID 추가 전달
                .toList();

        return CommentListResponse.Converter.from(post, comments, responses);
    }

    // 댓글 트리 생성
    private CommentResponse buildCommentTree(
            Comment comment,
            Map<Long, List<Comment>> childrenMap,
            Map<Long, List<FileInfoDTO>> fileMap,
            Map<Long, List<LinkInfoDTO>> linkMap,
            Long loginUserId
    ) {
        // 하위 댓글 재귀 처리 (메모리)
        List<CommentResponse> replies = childrenMap.getOrDefault(comment.getCommentId(), List.of())
                .stream()
                .map(child -> buildCommentTree(child, childrenMap, fileMap, linkMap, loginUserId)) // 재귀 전파
                .toList();

        return CommentResponse.Converter.from(
                comment,
                fileMap.getOrDefault(comment.getCommentId(), List.of()),
                linkMap.getOrDefault(comment.getCommentId(), List.of()),
                replies,
                loginUserId
        );
    }

    // 댓글 수정
    public CommentResponse updateComment(
            Long commentId, @Valid CommentUpdateRequest requestDto, String clientIp, Long userId){

        // 1. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_COMMENT_NOT_FOUND));

        // 2. 작성자 권한 검증 (작성자만 수정 가능)
        if(!comment.getUser().getId().equals(userId)){
            throw new BusinessException(ErrorCode.COMMENT_PERMISSION_DENIED);
        }

        // 3. 기본 필드 업데이트 (content)
        comment.updateContent(requestDto.getContent());

        // 4. 파일 삭제 처리 (removeFileIds가 제공된 경우)
        if (requestDto.getRemoveFileIds() != null && !requestDto.getRemoveFileIds().isEmpty()) {
            List<File> filesToDelete = fileRepository.findAllById(requestDto.getRemoveFileIds());

            // 파일이 해당 댓글에 속하는지 검증
            for (File file : filesToDelete) {
                if (file.getComment() == null || !file.getComment().getCommentId().equals(commentId)) {
                    throw new BusinessException(ErrorCode.FILE_NOT_IN_POST);
                }
            }

            // Soft Delete 적용
            for (File file : filesToDelete) {
                file.softDelete(userId);
            }
        }

        // 5. 파일 추가 처리 (addFileIds가 제공된 경우)
        if (requestDto.getAddFileIds() != null && !requestDto.getAddFileIds().isEmpty()) {
            fileService.saveFiles(comment, requestDto.getAddFileIds(), userId);
        }

        // 6. 링크 업데이트 (링크 URL이 제공된 경우)
        if (requestDto.getLinkUrls() != null) {
            // 기존 링크 조회 및 삭제
            List<Link> existingLinks = linkRepository.findByComment(comment);
            linkRepository.deleteAll(existingLinks);

            // 새 링크 저장
            linkService.saveLinks(comment, requestDto.getLinkUrls(), userId);
        }

        // 7. 저장된 데이터 재조회
        List<File> files = fileRepository.findFilesByCommentId(comment.getCommentId());
        List<Link> links = linkRepository.findLinksByCommentId(comment.getCommentId());

        return CommentResponse.Converter.from(
                comment, FileInfoDTO.Converter.from(files), LinkInfoDTO.Converter.from(links), List.of(),userId);
    }

    // 댓글 삭제 (soft delete)
    public void softDeleteComment(Long commentId, Long userId){

        // 1. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_COMMENT_NOT_FOUND));

        // 2. 작성자 권한 검증 (작성자만 삭제 가능)
        if(!comment.getUser().getId().equals(userId)){
            throw new BusinessException(ErrorCode.COMMENT_PERMISSION_DENIED);
        }

        // 3. 이미 삭제된 댓글 확인
        if (comment.getIsDeleted()) {
            throw new BusinessException(ErrorCode.COMMENT_ALREADY_DELETED);
        }
        // 소프트 삭제
        comment.softDelete(userId);
        commentRepository.save(comment);

        // 4. 댓글과 연결된 파일 삭제
        List<File> file = fileRepository.findFilesByCommentId(comment.getCommentId());
        for (File fileToDelete : file) {
            fileToDelete.softDelete(userId);
        }
    }
}
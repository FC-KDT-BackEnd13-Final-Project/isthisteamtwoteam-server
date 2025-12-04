package org.etmetmy.bn_server.domain.comment.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.dto.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.CommentListResponse;
import org.etmetmy.bn_server.domain.comment.dto.CommentResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.comment.repository.CommentRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨에서 트랜잭션 관리
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    //주 댓글 생성
    public void createComment(Long postId, CommentCreateRequest request, HttpServletRequest servletRequest) {

        // Post와 User 엔티티 유효성 검사 및 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        // IP 주소 추출
        String clientIp = servletRequest.getRemoteAddr();

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.getContent())
                .ip(clientIp)
                .commentId2(null)
                .build();

        commentRepository.save(comment);
    }

    //대댓글 (Reply)을 생성합니다.
    public void createReply(Long postId, Long parentCommentId, CommentCreateRequest request, HttpServletRequest servletRequest) {

        // 1. Post, User, Parent Comment 유효성 검사
        postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 부모 댓글 존재 여부 확인
        commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARENT_COMMENT_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        // IP 주소
        String clientIp = servletRequest.getRemoteAddr();

        // 2. Comment 엔티티 생성 (commentId2에 부모 ID 설정)
        Comment comment = Comment.builder()
                .post(postRepository.getReferenceById(postId))
                .user(user)
                .content(request.getContent())
                .ip(clientIp)
                .commentId2(parentCommentId)
                .build();

        commentRepository.save(comment);
    }

    //게시글 ID에 해당하는 모든 댓글 목록을 계층 구조로 조회
    @Transactional(readOnly = true)
    public CommentListResponse getCommentsByPostId(Long postId) {

        // 1. Post 존재 여부 확인 (외래 키 검증)
        postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 2. 최상위 댓글 조회
        List<Comment> rootComments = commentRepository.findRootCommentsByPostId(postId);

        // 3. 계층 구조 DTO 변환 및 대댓글 로딩
        List<CommentResponse> commentResponses = rootComments.stream()
                .map(this::mapToCommentResponseWithReplies) // 하위 메서드 호출
                .collect(Collectors.toList());

        return CommentListResponse.builder()
                .postId(postId)
                .totalCount(rootComments.size())
                .comments(commentResponses)
                .build();
    }

     //대댓글 (Reply)을 로딩하고 DTO로 변환
    private CommentResponse mapToCommentResponseWithReplies(Comment comment) {
        // 1. 대댓글 조회
        List<Comment> replies = commentRepository.findRepliesByParentId(comment.getCommentId());

        // 2. 대댓글 DTO로 변환
        List<CommentResponse> replyResponses = replies.stream()
                .map(this::mapToCommentResponseWithReplies)
                .collect(Collectors.toList());

        // 3. 현재 댓글 DTO 생성
        return CommentResponse.fromEntity(comment, replyResponses);
    }
}
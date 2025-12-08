package org.etmetmy.bn_server.domain.comment.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.dto.request.CommentCreateRequest;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentListResponse;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.comment.repository.CommentRepository;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BoardNotFoundException;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨에서 트랜잭션 관리
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    //주 댓글 생성
    public CommentResponse createComment(Long postId, CommentCreateRequest request, HttpServletRequest servletRequest) {

        // Post와 User 엔티티 유효성 검사 및 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        Comment comment = CommentCreateRequest.Converter.toEntity(post,request,servletRequest);

        Comment saved = commentRepository.save(comment);

        return CommentResponse.Converter.from(saved);
    }

    //대댓글 (Reply)을 생성합니다.
    public CommentResponse createReply(Long postId, Long parentCommentId, CommentCreateRequest request, HttpServletRequest servletRequest) {

        // Post와 User 엔티티 유효성 검사 및 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 부모 댓글 존재 여부 확인
        Comment parentComment =commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARENT_COMMENT_NOT_FOUND));

        Comment comment = CommentCreateRequest.Converter.toEntity(post,request,parentComment,servletRequest);
        Comment saved = commentRepository.save(comment);

        return CommentResponse.Converter.from(saved);
    }

    //게시글 ID에 해당하는 모든 댓글 목록을 계층 구조로 조회
    @Transactional(readOnly = true)
    public CommentListResponse getCommentsByPostId(Long postId) {

        // 1. Post 존재 여부 확인 (외래 키 검증)
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 2. 최상위 댓글 조회
        List<Comment> rootComments = commentRepository.findRootCommentsByPostId(postId);

        // 3. 계층 구조 DTO 변환 및 대댓글 로딩
        List<CommentResponse> commentResponses = rootComments.stream()
                .map(this::mapToCommentResponseWithReplies) // 하위 메서드 호출
                .collect(Collectors.toList());

        return CommentListResponse.Converter.from(post,rootComments,commentResponses);
    }

     //대댓글 (Reply)을 로딩하고 DTO로 변환
    public CommentResponse mapToCommentResponseWithReplies(Comment comment) {
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
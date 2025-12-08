package org.etmetmy.bn_server.domain.comment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentResponse {
    private final Long commentId;
    private final Long userId;
    private final String userName; // 댓글 작성자 이름
    private final String content;
    private final LocalDateTime createdAt;
    private final List<CommentResponse> replies; // 대댓글 목록

    public static class Converter{
        public static CommentResponse from(Comment comment) {
            return CommentResponse.builder()
                    .commentId(comment.getCommentId())
                    .userId(comment.getUser().getId())
                    .userName(comment.getUser().getName())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .build();
        }
    }
    // Entity -> DTO 변환 메서드
    public static CommentResponse fromEntity(Comment comment, List<CommentResponse> replies) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }
}
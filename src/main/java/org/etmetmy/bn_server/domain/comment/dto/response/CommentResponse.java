package org.etmetmy.bn_server.domain.comment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentResponse {
    private final Long commentId;
    private final Long parentId;
    private final Long userId;
    private final String userName; // 댓글 작성자 이름
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String ip;
    private final Boolean canEdit; //수정권한

    private final List<FileInfoDTO> files;
    private final List<LinkInfoDTO> links;
    private final List<CommentResponse> replies; // 대댓글 목록

    public static class Converter {
        public static CommentResponse from(
                Comment comment,
                List<FileInfoDTO> files,
                List<LinkInfoDTO> links,
                List<CommentResponse> replies,
                Long loginUserId) {

            return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .parentId(comment.getParent() != null ? comment.getParent().getCommentId() : null )
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getName())
                .content(comment.getContent())
                .ip(comment.getIp())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .canEdit(comment.getUser().getId().equals(loginUserId)) // 권한 검증 로직
                .files(files)
                .links(links)
                .replies(replies)
                .build();
        }
    }
}
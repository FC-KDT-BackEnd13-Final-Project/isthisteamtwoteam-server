package org.etmetmy.bn_server.domain.comment.dto.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.user.entity.User;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    private Long commentId2; // 대댓글인 경우 부모 댓글 ID, 일반 댓글인 경우 null

    public static class Converter{
        public static Comment toEntity(Post post, CommentCreateRequest request, HttpServletRequest servletRequest, User user) {
            return Comment.builder()
                    .post(post)
                    .user(user)
                    .content(request.getContent())
                    .ip(servletRequest.getRemoteAddr())
                    .commentId2(request.getCommentId2())
                    .isDeleted(false)
                    .build();
        }
    }
}
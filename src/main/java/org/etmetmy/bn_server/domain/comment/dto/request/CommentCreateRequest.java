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

    @NotNull(message = "작성자 ID는 필수입니다.")
    private User userId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    public static class Converter{
        public static Comment toEntity(Post post, CommentCreateRequest request, HttpServletRequest servletRequest) {
            return Comment.builder()
                    .post(post)
                    .user(request.getUserId())
                    .content(request.getContent())
                    .ip(servletRequest.getRemoteAddr())
                    .commentId2(null)
                    .build();
        }

        public static Comment toEntity(Post post, CommentCreateRequest request,Comment parentComment, HttpServletRequest servletRequest) {
            return Comment.builder()
                    .post(post)
                    .user(request.getUserId())
                    .content(request.getContent())
                    .ip(servletRequest.getRemoteAddr())
                    .commentId2(parentComment.getCommentId())
                    .build();
        }

    }
}
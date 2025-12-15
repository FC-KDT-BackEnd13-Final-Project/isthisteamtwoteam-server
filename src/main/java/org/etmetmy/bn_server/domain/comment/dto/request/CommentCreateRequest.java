package org.etmetmy.bn_server.domain.comment.dto.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.util.List;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    private Long parentId;

    // S3에 업로드된 파일 정보 목록 (선택 사항)
    private List<Long> fileIds;

    // 링크 URL 목록 (선택 사항)
    private List<String> linkUrls;

    public static class Converter {
        public static Comment toEntity(Post post, CommentCreateRequest request, User user, String clientIp) {
            Comment parentComment = null;
            if (request.getParentId() != null) {
                parentComment = Comment.builder().commentId(request.getParentId()).build();
            }

            return Comment.builder()
                    .post(post)
                    .user(user)
                    .content(request.getContent())
                    .ip(clientIp)
                    .parent(parentComment)
                    .isDeleted(false)
                    .build();
        }
    }
}
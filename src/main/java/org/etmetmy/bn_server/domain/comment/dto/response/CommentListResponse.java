package org.etmetmy.bn_server.domain.comment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.post.entity.Post;

import java.util.List;

@Getter
@Builder
public class CommentListResponse {
    private final Long postId;
    private final int totalCount;
    private final List<CommentResponse> comments;

    public static class Converter{
        public static CommentListResponse from(Post post,
                                               List<Comment> allComments,
                                               List<CommentResponse> commentResponse) {
            return CommentListResponse.builder()
                    .postId(post.getPostId())
                    .totalCount(allComments.size())
                    .comments(commentResponse)
                    .build();
        }
    }
}
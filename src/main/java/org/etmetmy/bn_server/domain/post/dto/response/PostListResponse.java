package org.etmetmy.bn_server.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private Long postNumber;
    private Long postId;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private Boolean isCompleted;
    private Long stageId;
    private String stageName;
    private String createdIp;
    private Long parentId;

    public static PostListResponse from(Post post) {
        return PostListResponse.builder()
                .postNumber(post.getPostNumber())  // ← Post 엔티티에서 가져오기
                .postId(post.getPostId())
                .authorId(post.getAuthorId())
                .authorName(post.getUser().getName())
                .title(post.getTitle())
                .content(post.getContent())
                .isCompleted(post.getIsCompleted())
                .stageId(post.getStage().getStageId())
                .stageName(post.getStage().getStageName())
                .createdIp(post.getCreatedIp())
                .parentId(post.getParentPostId())
                .build();
    }
}
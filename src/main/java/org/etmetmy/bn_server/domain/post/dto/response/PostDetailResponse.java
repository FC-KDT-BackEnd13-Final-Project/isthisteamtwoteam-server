package org.etmetmy.bn_server.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.user.entity.User; // User Entity import
import java.time.LocalDateTime;

@Getter
@Builder
public class PostDetailResponse {
    private final Long postId;
    private final Long parentPostId; // null or Id번호
    private final Long userId;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Boolean isCompleted;
    private final String stageName;
    private final String message;


    public static PostDetailResponse fromEntity(Post post, User author) {

        String calculatedStageName = (post.getStage().getStageName() != null)
                ? post.getStage().getStageName() // ID가 있으면 "ID: 99" 등으로 표시
                : "미정";

        return PostDetailResponse.builder()
                .postId(post.getPostId())
                .parentPostId(post.getParentPostId())
                .userId(author.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isCompleted(post.getIsCompleted())
                .stageName(calculatedStageName)
                .message("게시글을 성공적으로 조회했습니다.")
                .build();
    }
}
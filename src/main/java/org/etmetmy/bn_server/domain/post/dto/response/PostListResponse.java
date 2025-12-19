package org.etmetmy.bn_server.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;

import java.time.LocalDateTime;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private Long postNumber;
    private Long postId;
    private String authorName;
    private String title;
    private String content;
    private Boolean isCompleted;
    private Long stageId;
    private String stageName;
    private String createdIp;
    private Long parentId;
    private String approveStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    public static PostListResponse from(Post post, Request request) {
        return PostListResponse.builder()
                .postNumber(post.getPostNumber())  // ← Post 엔티티에서 가져오기
                .postId(post.getPostId())
                .authorName(post.getUser().getName())
                .title(post.getTitle())
                .content(post.getContent())
                .isCompleted(post.getIsCompleted())
                .stageId(post.getStage().getId())
                .stageName(post.getStage().getStageName())
                .createdIp(post.getCreatedIp())
                .parentId(post.getParentPostId())
                .approveStatus(request != null && request.getApproveStatus() != null ? request.getApproveStatus().getDescription() : null)
                .createdAt(post.getCreatedAt())
                .build();
    }

    // 기존 호환성을 위한 from 메서드 (Request 없이)
    public static PostListResponse from(Post post) {
        return from(post, null);
    }
}
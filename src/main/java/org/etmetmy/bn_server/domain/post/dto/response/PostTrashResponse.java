package org.etmetmy.bn_server.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTrashResponse {
    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("uploaded_by")
    private Long uploadedUserId;

    @JsonProperty("uploaded_user_name")
    private String uploadedUserName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    public static class Converter {
        public static PostTrashResponse from(Post post) {
            return PostTrashResponse.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .uploadedUserId(post.getUser().getId())
                    .uploadedUserName(post.getUser().getName())
                    .createdAt(post.getCreatedAt())
                    .deletedAt(post.getDeletedAt())
                    .build();
        }
        public static List<PostTrashResponse> from(List<Post> posts) {
            return posts.stream()
                    .map(PostTrashResponse.Converter::from)
                    .collect(Collectors.toList());
        }
    }
}

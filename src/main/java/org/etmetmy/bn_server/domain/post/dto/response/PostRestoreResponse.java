package org.etmetmy.bn_server.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRestoreResponse {

    @JsonProperty("restored_count")
    private Long restoredCount;

    @JsonProperty("restored_ids")
    private List<Long> restoredIds;

    public static class Converter {
        public static PostRestoreResponse from(List<Post> posts) {

            return PostRestoreResponse.builder()
                    .restoredCount((long)posts.size())
                    .restoredIds(posts.stream().map(Post::getPostId).toList())
                    .build();
        }
    }
}
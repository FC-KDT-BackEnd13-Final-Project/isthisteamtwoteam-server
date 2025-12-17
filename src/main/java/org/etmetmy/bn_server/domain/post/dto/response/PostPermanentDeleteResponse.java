package org.etmetmy.bn_server.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostPermanentDeleteResponse {

    @JsonProperty("deleted_count")
    private Long deletedCount;

    @JsonProperty("deleted_ids")
    private List<Long> deletedIds;


    public static class Converter {
        public static PostPermanentDeleteResponse from(List<Post> posts) {

            return PostPermanentDeleteResponse.builder()
                    .deletedCount((long)posts.size())
                    .deletedIds(posts.stream().map(Post::getPostId).toList())
                    .build();
        }
    }
}

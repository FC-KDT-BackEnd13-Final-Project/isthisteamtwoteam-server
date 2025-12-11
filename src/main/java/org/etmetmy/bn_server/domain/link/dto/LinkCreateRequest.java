package org.etmetmy.bn_server.domain.link.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.post.entity.Post;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkCreateRequest {

    private String linkUrl;

    public static class Converter {

        // Link 엔티티 생성 (post용)
        public static Link toEntity(Post post, String url, Long uploadedBy) {
            return Link.builder()
                    .post(post)
                    .linkUrl(url)
                    .uploadedBy(uploadedBy)
                    .isDeleted(false)
                    .build();
        }

        public static List<Link> toEntity(Post post, List<String> linkUrls, Long uploadedBy) {
            return linkUrls.stream()
                    .map(url -> Link.builder()
                            .post(post)
                            .linkUrl(url)
                            .uploadedBy(uploadedBy)
                            .isDeleted(false)
                            .build())
                    .collect(Collectors.toList());
        }
    }
}
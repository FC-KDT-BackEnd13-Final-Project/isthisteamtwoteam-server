package org.etmetmy.bn_server.domain.link.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
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
        // 공통 생성 로직
        private static Link buildLink(Post post, Comment comment, String url, Long uploadedBy) {
            return Link.builder()
                    .post(post)
                    .comment(comment)
                    .linkUrl(url)
                    .uploadedBy(uploadedBy)
                    .isDeleted(false)
                    .build();
        }

        // post 단건
        public static Link toEntity(Post post, String url, Long uploadedBy) {
            return buildLink(post, null, url, uploadedBy);
        }

        // comment 단건
        public static Link toEntity(Comment comment, String url, Long uploadedBy) {
            return buildLink(null, comment, url, uploadedBy);
        }

        // post 리스트
        public static List<Link> toEntity(Post post, List<String> linkUrls, Long uploadedBy) {
            return linkUrls.stream()
                    .map(url -> buildLink(post, null, url, uploadedBy))
                    .collect(Collectors.toList());
        }

        // comment 리스트
        public static List<Link> toEntity(Comment comment, List<String> linkUrls, Long uploadedBy) {
            return linkUrls.stream()
                    .map(url -> buildLink(null, comment, url, uploadedBy))
                    .collect(Collectors.toList());
        }
    }
}
package org.etmetmy.bn_server.domain.link.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.EntityType;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.post.entity.Post;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkCreateRequest {

    private String linkUrl;

    public static class Converter {
        /**
         * Link 엔티티 생성 (Post용)
         */
        public static Link toEntity(String linkUrl, Post post, Long uploadedBy) {
            return Link.builder()
                    .entityType(EntityType.builder().entityTypeId(1L).build()) // 1L = Post 타입
                    .post(post)
                    .linkUrl(linkUrl)
                    .uploadedBy(uploadedBy)
                    .isDeleted(false)
                    .build();
        }
    }
}
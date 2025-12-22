package org.etmetmy.bn_server.domain.link.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;

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
        private static Link buildLink(Post post, Comment comment, ProjectCheckList projectCheckList, Request request, String url, Long uploadedBy) {
            return Link.builder()
                    .post(post)
                    .comment(comment)
                    .projectCheckList(projectCheckList)
                    .linkUrl(url)
                    .request(request)
                    .uploadedBy(uploadedBy)
                    .isDeleted(false)
                    .build();
        }

        // post 단건
        public static Link toEntity(Post post, String url, Long uploadedBy) {
            return buildLink(post, null, null, null, url, uploadedBy);
        }

        // comment 단건
        public static Link toEntity(Comment comment, String url, Long uploadedBy) {
            return buildLink(null, comment, null, null, url, uploadedBy);
        }

        // checkList 단건
        public static Link toEntity(ProjectCheckList projectCheckList, String url, Long uploadedBy) {
            return buildLink(null, null, projectCheckList, null, url, uploadedBy);
        }

        // request 단건
        public static Link toEntity(Request request, String url, Long uploadedBy) {
            return buildLink(null, null, null, request, url, uploadedBy);
        }

        // post 리스트
        public static List<Link> toEntity(Post post, List<String> linkUrls, Long uploadedBy) {
            return linkUrls.stream()
                    .map(url -> buildLink(post, null, null, null, url, uploadedBy))
                    .collect(Collectors.toList());
        }

        // comment 리스트
        public static List<Link> toEntity(Comment comment, List<String> linkUrls, Long uploadedBy) {
            return linkUrls.stream()
                    .map(url -> buildLink(null, comment, null, null, url, uploadedBy))
                    .collect(Collectors.toList());
        }

        // projectCheckList 리스트
        public static List<Link> toEntity(ProjectCheckList projectCheckList, List<String> linkUrls, Long uploadedBy) {
            return linkUrls.stream()
                    .map(url -> buildLink(null, null, projectCheckList, null, url, uploadedBy))
                    .collect(Collectors.toList());
        }

        // request 리스트
        public static List<Link> toEntity(Request request, List<String> linkUrls, Long uploadedBy) {
            return linkUrls.stream()
                    .map(url -> buildLink(null, null, null, request, url, uploadedBy))
                    .collect(Collectors.toList());
        }
    }
}
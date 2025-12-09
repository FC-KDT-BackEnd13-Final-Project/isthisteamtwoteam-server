package org.etmetmy.bn_server.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyPostCreateResponse {

    @JsonProperty("postId")
    private Long postId;

    @JsonProperty("parentPostId")
    private Long parentPostId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    @JsonProperty("stageId")
    private Long stageId;

    @JsonProperty("createdByUserId")
    private Long createdByUserId;

    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    @JsonProperty("files")
    private List<FileInfoDTO> files;

    @JsonProperty("linkUrls")
    private List<String> linkUrls;


    public static class Converter {
        public static ReplyPostCreateResponse from(
                Post post, Post parentPost, List<File> files, List<Link> links
        ) {
            // 파일 목록 변환 (삭제되지 않은 파일만)
            List<FileInfoDTO> fileInfos = FileInfoDTO.Converter.from(files);

            // 링크 목록 변환 (삭제되지 않은 링크만) - LinkInfoDTO 사용
            List<String> linkUrls = LinkInfoDTO.Converter.toUrlList(links);

            return ReplyPostCreateResponse.builder()
                    .postId(post.getPostId())
                    .parentPostId(parentPost.getPostId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .stageId(post.getStage().getId())
                    .createdByUserId(post.getUser().getId())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .files(fileInfos)
                    .linkUrls(linkUrls)
                    .build();
        }
    }
}
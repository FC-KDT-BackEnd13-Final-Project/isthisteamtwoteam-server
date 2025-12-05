package org.etmetmy.bn_server.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.FileInfoDTO;
import org.etmetmy.bn_server.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateResponse {

    @JsonProperty("postId")
    private Long postId;

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

    @JsonProperty("files")
    private List<FileInfoDTO> files;

    @JsonProperty("linkUrls")
    private List<String> linkUrls;


    public static class Converter {
        public static PostCreateResponse from(
                Post post,
                List<org.etmetmy.bn_server.domain.file.entity.File> files,
                List<String> linkUrls) {

            // 파일 목록 변환 (삭제되지 않은 파일만)
            List<FileInfoDTO> fileInfos = FileInfoDTO.Converter.from(files);

            // 링크 URL 목록 안전하게 처리
            List<String> safeLinkUrls = linkUrls != null ? linkUrls : new ArrayList<>();

            return PostCreateResponse.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .stageId(post.getStage().getId())
                    .createdByUserId(post.getUser().getId())
                    .createdAt(post.getCreatedAt())
                    .files(fileInfos)
                    .linkUrls(safeLinkUrls)
                    .build();
        }
    }
}
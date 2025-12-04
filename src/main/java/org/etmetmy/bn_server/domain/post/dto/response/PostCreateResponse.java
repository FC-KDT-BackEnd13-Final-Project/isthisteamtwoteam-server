package org.etmetmy.bn_server.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private List<FileInfo> files;

    @JsonProperty("linkUrls")
    private List<String> linkUrls;

    /**
     * 파일 정보 DTO (화면 표시용: 파일명, 용량 포함)
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FileInfo {
        @JsonProperty("fileId")
        private Long fileId;

        @JsonProperty("fileName")
        private String fileName;

        @JsonProperty("fileSize")
        private Long fileSize;

        @JsonProperty("fileUrl")
        private String fileUrl;

        @JsonProperty("fileType")
        private String fileType;
    }

    public static class Converter {
        public static PostCreateResponse from(
                Post post,
                List<org.etmetmy.bn_server.domain.file.entity.File> files,
                List<String> linkUrls) {

            // 파일 목록 변환 (삭제되지 않은 파일만)
            List<FileInfo> fileInfos = convertFilesToFileInfos(files);

            // 링크 URL 목록 안전하게 처리
            List<String> safeLinkUrls = linkUrls != null ? linkUrls : new ArrayList<>();

            return PostCreateResponse.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .stageId(post.getStage().getStageId())
                    .createdByUserId(post.getUser().getId())
                    .createdAt(post.getCreatedAt())
                    .files(fileInfos)
                    .linkUrls(safeLinkUrls)
                    .build();
        }

        /**
         * File 엔티티 리스트를 FileInfo DTO 리스트로 변환
         * 삭제된 파일은 제외
         */
        private static List<FileInfo> convertFilesToFileInfos(
                List<org.etmetmy.bn_server.domain.file.entity.File> files) {

            // 1. files가 null이면 빈 리스트 반환
            if (files == null) {
                return new ArrayList<>();
            }

            // 2. 변환된 FileInfo를 담을 리스트 생성
            List<FileInfo> fileInfos = new ArrayList<>();

            // 3. 각 File을 순회하면서 FileInfo로 변환
            for (org.etmetmy.bn_server.domain.file.entity.File file : files) {
                // 삭제된 파일은 건너뛰기
                if (file.getIsDeleted()) {
                    continue;
                }

                // File 엔티티의 정보를 FileInfo DTO로 변환
                FileInfo fileInfo = FileInfo.builder()
                        .fileId(file.getFileId())
                        .fileName(file.getFileTitle())
                        .fileSize(file.getFileSize())
                        .fileUrl(file.getFilePath())
                        .fileType(file.getFileType())
                        .build();

                // 리스트에 추가
                fileInfos.add(fileInfo);
            }

            return fileInfos;
        }
    }
}
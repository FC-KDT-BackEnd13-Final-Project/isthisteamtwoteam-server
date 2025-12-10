package org.etmetmy.bn_server.domain.file.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.entity.Post;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileCreateRequest {

    private String fileUrl;


    public static class Converter {

        // File 엔티티 생성
        public static File toEntity(String fileUrl, Long fileSize, Post post, Long uploadedBy) {
            return File.builder()
                    .post(post)
                    .fileTitle(extractFileName(fileUrl))
                    .filePath(fileUrl)
                    .fileSize(fileSize)
                    .fileType(extractFileType(fileUrl))
                    .uploadedBy(uploadedBy)
                    .isDeleted(false)
                    .build();
        }

        // URL 에서 파일명 추출
        public static String extractFileName(String url) {
            int lastSlash = url.lastIndexOf('/');
            return lastSlash >= 0 ? url.substring(lastSlash + 1) : "unknown";
        }

        // URL 에서 파일 확장자 추출
        public static String extractFileType(String url) {
            int lastDot = url.lastIndexOf('.');
            return lastDot >= 0 ? url.substring(lastDot + 1).toLowerCase() : "unknown";
        }
    }
}
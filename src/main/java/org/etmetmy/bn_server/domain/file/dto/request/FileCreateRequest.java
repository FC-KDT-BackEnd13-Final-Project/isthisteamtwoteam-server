package org.etmetmy.bn_server.domain.file.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileCreateRequest {

    private String fileUrl;

    public static class Converter {

        // 임시 파일 엔티티 생성 (MultipartFile 기반)
        public static File toEntity(Post post, String fileUrl, MultipartFile file, Long uploadedBy) {
            return File.builder()
                    .post(post)
                    .fileTitle(extractFileName(fileUrl))
                    .filePath(fileUrl) // S3 저장 경로
                    .fileSize(file.getSize())
                    .fileType(extractFileType(fileUrl))
                    .uploadedBy(uploadedBy)
                    .isTemp(true)
                    .isDeleted(false)
                    .build();
        }

        // URL 에서 파일명 추출
        public static String extractFileName(String url) {
            if (url == null || url.isEmpty()) return "unknown";

            // 1. 쿼리 제거
            String path = url.split("\\?")[0];

            // 2. 마지막 슬래시 이후 추출
            int lastSlash = path.lastIndexOf('/');
            return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        }

        // URL 에서 파일 확장자 추출
        public static String extractFileType(String url) {
            if (url == null || url.isEmpty()) return "unknown";

            String path = url.split("\\?")[0]; // 쿼리 제거
            int lastDot = path.lastIndexOf('.');
            return lastDot >= 0 ? path.substring(lastDot + 1).toLowerCase() : "unknown";
        }
    }
}
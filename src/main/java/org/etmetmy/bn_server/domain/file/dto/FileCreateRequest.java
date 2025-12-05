package org.etmetmy.bn_server.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.EntityType;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.entity.Post;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileCreateRequest {

    private String fileUrl;

    /**
     * File 엔티티 생성
     * @param fileUrl S3에 업로드된 파일 URL
     * @param post 연결된 게시글
     * @param uploadedBy 업로드한 사용자 ID
     * @return File 엔티티
     */
    public static File toEntity(String fileUrl, Post post, Long uploadedBy) {
        return File.builder()
                .entityType(EntityType.builder().entityTypeId(1L).build())
                .post(post)
                .fileTitle(extractFileName(fileUrl))
                .filePath(fileUrl)
                .fileSize(0L)
                .fileType(extractFileType(fileUrl))
                .uploadedBy(uploadedBy)
                .isDeleted(false)
                .build();
    }

    /**
     * URL에서 파일명 추출
     * @param url 파일 URL
     * @return 파일명
     */
    private static String extractFileName(String url) {
        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : "unknown";
    }

    /**
     * URL에서 파일 확장자 추출
     * @param url 파일 URL
     * @return 파일 확장자
     */
    private static String extractFileType(String url) {
        int lastDot = url.lastIndexOf('.');
        return lastDot >= 0 ? url.substring(lastDot + 1).toLowerCase() : "unknown";
    }
}
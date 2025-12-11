package org.etmetmy.bn_server.domain.file.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileCreateRequest {

    private String fileUrl;


    public static class Converter {

        // 다중 File 엔티티 생성
        public static List<File> toEntity(Post post, List<PostCreateRequest.FileInfo> fileInfos, Long uploadedBy) {
            return fileInfos.stream()
                    .map(fileInfo -> File.builder()
                            .post(post)
                            .fileTitle(extractFileName(fileInfo.getFileUrl()))
                            .filePath(fileInfo.getFileUrl())
                            .fileSize(fileInfo.getFileSize())
                            .fileType(extractFileType(fileInfo.getFileUrl()))
                            .uploadedBy(uploadedBy)
                            .isDeleted(false)
                            .build())
                    .collect(Collectors.toList());
        }

        // 단일 파일 엔티티 생성 (MultipartFile 기반)
        public static File toEntity(Post post, String fileUrl, MultipartFile file, Long uploadedBy) {
            return File.builder()
                    .post(post)
                    .fileTitle(extractFileName(fileUrl))  // URL에서 파일명 추출
                    .filePath(fileUrl)                     // S3 저장 경로
                    .fileSize(file.getSize())              // 파일 크기
                    .fileType(extractFileType(fileUrl))   // URL에서 확장자 추출
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
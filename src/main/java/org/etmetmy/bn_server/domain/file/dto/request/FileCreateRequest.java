package org.etmetmy.bn_server.domain.file.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.response.S3UploadResult;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.project.entity.Project;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileCreateRequest {

    private String fileUrl;

    public static class Converter {

        // 임시 파일 엔티티 생성 (S3UploadResult 기반)
        public static File toEntity(Project project, S3UploadResult uploadResult, Long uploadedBy) {
            return File.builder()
                    .project(project)
                    .s3FileTitle(uploadResult.getStoredFileName())       // UUID_원본파일명
                    .originalFileTitle(uploadResult.getOriginalFileName()) // 원본 파일명
                    .filePath(uploadResult.getFileUrl())                  // S3 저장 경로
                    .fileSize(uploadResult.getFileSize())
                    .fileType(uploadResult.getFileType())
                    .uploadedBy(uploadedBy)
                    .isTemp(true)
                    .isDeleted(false)
                    .build();
        }
    }
}
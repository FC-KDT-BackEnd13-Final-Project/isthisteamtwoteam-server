package org.etmetmy.bn_server.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.EntityType;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.entity.Post;

import static org.etmetmy.bn_server.domain.file.service.FileServiceImpl.extractFileName;
import static org.etmetmy.bn_server.domain.file.service.FileServiceImpl.extractFileType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileCreateRequest {

    private String fileUrl;
    private Long entityTypeId;


    public static class Converter {
        /**
         * File 엔티티 생성
         */
        public static File toEntity(FileCreateRequest fileCreateRequest, Post post, Long uploadedBy) {
            return File.builder()
                    .entityType(EntityType.builder().entityTypeId(fileCreateRequest.getEntityTypeId()).build())
                    .post(post)
                    .fileTitle(extractFileName(fileCreateRequest.getFileUrl()))
                    .filePath(fileCreateRequest.getFileUrl())
                    .fileSize(0L)
                    .fileType(extractFileType(fileCreateRequest.getFileUrl()))
                    .uploadedBy(uploadedBy)
                    .isDeleted(false)
                    .build();
        }
    }
}
package org.etmetmy.bn_server.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
파일 목록 조회 DTO
*/

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveFileListDTO {

    private Long fileId;
    private String fileTitle;
    private String filePath;
    private String fileType;
    private Long fileSize;

    private String entityTypeName;

    private Long postId;

    private Long uploadUserId;
    private LocalDateTime uploadedAt;

    // Converter 클래스
    public static class Converter {
        public static ActiveFileListDTO from(File file) {
            return ActiveFileListDTO.builder()
                    .fileId(file.getFileId())
                    .fileTitle(file.getFileTitle())
                    .filePath(file.getFilePath())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .entityTypeName(file.getEntityType().getEntityType())
                    .postId(file.getPost().getPostId())
                    .uploadUserId(file.getUploadedBy())
                    .uploadedAt(file.getCreatedAt())
                    .build();
        }

        public static List<ActiveFileListDTO> from(List<File> files) {
            return files.stream()
                    .map(Converter::from)
                    .collect(Collectors.toList());
        }
    }
}

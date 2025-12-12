package org.etmetmy.bn_server.domain.file.dto.response;

/*
임시파일 목록 DTO
*/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempFileListDTO {

    private Long fileId;
    private String fileTitle;
    private String filePath;
    private String fileType;
    private Long fileSize;

    private Long uploadUserId;
    private LocalDateTime uploadedAt;

    // Converter 클래스
    public static class Converter {
        public static TempFileListDTO from(File file) {
            return TempFileListDTO.builder()
                    .fileId(file.getFileId())
                    .fileTitle(file.getFileTitle())
                    .filePath(file.getFilePath())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .uploadUserId(file.getUploadedBy())
                    .uploadedAt(file.getCreatedAt())
                    .build();
        }

        public static List<TempFileListDTO> from(List<File> files) {
            return files.stream()
                    .map(TempFileListDTO.Converter::from)
                    .collect(Collectors.toList());
        }
    }

}

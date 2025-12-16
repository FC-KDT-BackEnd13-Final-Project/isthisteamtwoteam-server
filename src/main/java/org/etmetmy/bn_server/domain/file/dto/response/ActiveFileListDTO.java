package org.etmetmy.bn_server.domain.file.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String fileOriginalFileName;
    private String filePath;
    private String fileType;
    private Long fileSize;

    private String postTitle;
    private Long postId;

    private String uploadUserName;
    private Long uploadUserId;
    private LocalDateTime uploadedAt;

    // Converter 클래스
    public static class Converter {
        public static ActiveFileListDTO from(File file) {
            return ActiveFileListDTO.builder()
                    .fileId(file.getFileId())
                    .fileOriginalFileName(file.getOriginalFileTitle())
                    .filePath(file.getFilePath())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .postTitle(file.getPost().getTitle())
                    .postId(file.getPost().getPostId())
                    .uploadUserName(file.getPost().getUser().getName())
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

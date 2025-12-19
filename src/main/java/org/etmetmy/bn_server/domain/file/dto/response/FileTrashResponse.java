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

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileTrashResponse {

    @JsonProperty("file_id")
    private Long fileId;

    @JsonProperty("original_file_title")
    private String originalFileTitle;

    @JsonProperty("file_size")
    private String fileSize;

    @JsonProperty("uploaded_by")
    private Long uploadedUserId;

    @JsonProperty("uploaded_user_name")
    private String uploadedUserName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    public static class Converter {
        public static FileTrashResponse from(File file) {
            return FileTrashResponse.builder()
                    .fileId(file.getFileId())
                    .originalFileTitle(file.getOriginalFileTitle())
                    .fileSize(file.getFileSize())
                    .uploadedUserId(file.getUploadedBy())
                    .uploadedUserName(file.getUploader().getName())
                    .createdAt(file.getCreatedAt())
                    .deletedAt(file.getDeletedAt())
                    .build();
        }
        public static List<FileTrashResponse> from(List<File> files) {
            return files.stream()
                    .map(FileTrashResponse.Converter::from)
                    .collect(Collectors.toList());
        }
    }
}

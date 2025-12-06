package org.etmetmy.bn_server.domain.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTrashResponse {

    private Long fileId;
    private String fileTitle;
    private String filePath;
    private String fileType;
    private Long fileSize;

    private Long postId;

    private String uploadUserName;
    private Long uploadUserId;
    private LocalDateTime uploadedAt;

    private LocalDateTime deletedAt;


    public static class Converter {
        public static FileTrashResponse from(File file) {
            return FileTrashResponse.builder()
                    .fileId(file.getFileId())
                    .fileTitle(file.getFileTitle())
                    .filePath(file.getFilePath())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .postId(file.getPost().getPostId())
                    .uploadUserId(file.getUploadedBy()) // 업로드한 사용자 ID
                    .uploadedAt(file.getCreatedAt())
                    .deletedAt(file.getDeletedAt())
                    .build();
        }
    }
}

package org.etmetmy.bn_server.domain.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;

import java.time.LocalDateTime;

// todo: project 상세 조회 시 필요한 파일 목록
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileListResponse {
    private Long fileId;
    private String fileTitle;
    private String fileSize;
    private String filePath;

    private String uploadUserName;
    private LocalDateTime uploadedAt;

    public static FileListResponse from(File file) {
        // 업로드한 사용자 이름 결정 (Post -> Comment -> ProjectCheckList 순서로 확인)
        String uploadUserName = null;

        if (file.getPost() != null && file.getPost().getUser() != null) {
            uploadUserName = file.getPost().getUser().getName();
        } else if (file.getComment() != null && file.getComment().getUser() != null) {
            uploadUserName = file.getComment().getUser().getName();
        } else if (file.getProjectCheckList() != null && file.getProjectCheckList().getAnswererId() != null) {
            uploadUserName = file.getProjectCheckList().getAnswererId().getName();
        }

        return FileListResponse.builder()
                .fileId(file.getFileId())
                .fileTitle(file.getOriginalFileTitle())
                .fileSize(file.getFileSize())
                .filePath(file.getFilePath())
                .uploadUserName(uploadUserName)
                .uploadedAt(file.getCreatedAt())
                .build();
    }
}

package org.etmetmy.bn_server.domain.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.dto.response.PostListResponse;
import org.etmetmy.bn_server.domain.post.entity.Post;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileListResponse {
    private Long fileId;
    private String fileTitle;
    private Long fileSize;
    private String filePath;

    public static FileListResponse from(File file) {
        return FileListResponse.builder()
                .fileId(file.getFileId())
                .fileTitle(file.getOriginalFileTitle())
                .fileSize(file.getFileSize())
                .filePath(file.getFilePath())
                .build();
    }
}

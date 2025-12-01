package org.etmetmy.bn_server.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/*
파일 목록 조회 DTO
*/

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileListDTO {

    private Long fileId;
    private String fileTitle;
    private String filePath;
    private String fileType;
    private Long fileSize;

    private String entityTypeName;

    private Long postId;

    private Long uploadUserId;
    private LocalDateTime uploadedAt;

    private Boolean Deleted;
    private LocalDateTime deletedAt;
    private Long deleteUserId;
}

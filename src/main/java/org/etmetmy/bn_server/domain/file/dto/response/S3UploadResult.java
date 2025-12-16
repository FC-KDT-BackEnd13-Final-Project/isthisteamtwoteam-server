package org.etmetmy.bn_server.domain.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3UploadResult {
    private String originalFileName;  // 사용자가 업로드한 원본 파일명
    private String storedFileName;     // S3에 저장된 파일명 (UUID_원본파일명)
    private String fileUrl;            // S3 파일 URL
    private Long fileSize;             // 파일 크기
    private String fileType;           // 파일 타입
}
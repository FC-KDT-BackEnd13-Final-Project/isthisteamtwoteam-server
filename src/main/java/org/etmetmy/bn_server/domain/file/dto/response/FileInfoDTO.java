package org.etmetmy.bn_server.domain.file.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;

import java.util.ArrayList;
import java.util.List;

/**
 * 파일 정보 DTO (화면 표시용: 파일명, 용량 포함)
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfoDTO {

    @JsonProperty("fileId")
    private Long fileId;

    @JsonProperty("fileOriginalFileName")
    private String fileOriginalFileName;

    @JsonProperty("fileSize")
    private Long fileSize;

    @JsonProperty("fileUrl")
    private String fileUrl;

    @JsonProperty("fileType")
    private String fileType;

    /**
     * File 엔티티 리스트를 FileInfo DTO 리스트로 변환
     * 삭제된 파일은 제외
     */
    public static class Converter {

        public static List<FileInfoDTO> from(List<File> files) {
            // 1. files가 null 이면 빈 리스트 반환
            if (files == null) {
                return new ArrayList<>();
            }

            // 2. 변환된 FileInfo를 담을 리스트 생성
            List<FileInfoDTO> fileInfos = new ArrayList<>();

            // 3. 각 File을 순회하면서 FileInfo로 변환
            for (org.etmetmy.bn_server.domain.file.entity.File file : files) {
                // 삭제된 파일은 건너뛰기
                if (file.getIsDeleted()) {
                    continue;
                }
                // File 엔티티의 정보를 FileInfo DTO로 변환
                fileInfos.add(FileInfoDTO.builder()
                        .fileId(file.getFileId())
                        .fileOriginalFileName(file.getOriginalFileTitle())
                        .fileSize(file.getFileSize())
                        .fileUrl(file.getFilePath())
                        .fileType(file.getFileType())
                        .build());
            }
            return fileInfos;
        }
    }
}
package org.etmetmy.bn_server.domain.file.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedFileDto {

    @JsonProperty("fileId")
    private Long fileId;

    public static class Converter {
        // 해당 게시글에 연결된 모든 파일의 ID만 들어있는 DTO 리스트
        public static List<UploadedFileDto> from(Post post) {
            if (post.getFiles() == null || post.getFiles().isEmpty())
                return List.of();

            return post.getFiles().stream()
                    .map(file -> UploadedFileDto.builder()
                            .fileId(file.getFileId())
                            .build())
                    .toList();
        }
    }
}

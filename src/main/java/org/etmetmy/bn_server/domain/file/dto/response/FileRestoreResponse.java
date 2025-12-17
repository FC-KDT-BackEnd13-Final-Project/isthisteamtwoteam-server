package org.etmetmy.bn_server.domain.file.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileRestoreResponse {

    @JsonProperty("restored_count")
    private Long restoredCount;

    @JsonProperty("restored_ids")
    private List<Long> restoredIds;

    @JsonProperty("user_id")
    private Long userId;

    public static class Converter {
        public static FileRestoreResponse from(List<File> files, Long userId) {

            return FileRestoreResponse.builder()
                    .restoredCount((long)files.size())
                    .restoredIds(files.stream().map(File::getFileId).toList())
                    .userId(userId)
                    .build();
        }
    }
}

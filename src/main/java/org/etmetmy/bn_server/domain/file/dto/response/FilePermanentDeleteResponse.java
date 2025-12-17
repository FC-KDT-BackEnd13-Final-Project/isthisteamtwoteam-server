package org.etmetmy.bn_server.domain.file.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.file.entity.File;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilePermanentDeleteResponse {

    @JsonProperty("deleted_count")
    private Long deletedCount;

    @JsonProperty("deleted_ids")
    private List<Long> deletedIds;


    public static class Converter {
        public static FilePermanentDeleteResponse from(List<File> files) {

            return FilePermanentDeleteResponse.builder()
                    .deletedCount((long)files.size())
                    .deletedIds(files.stream().map(File::getFileId).toList())
                    .build();
        }
    }
}

package org.etmetmy.bn_server.domain.file.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileRestoreRequest {

    @JsonProperty("file_ids")
    private List<Long> fileIds;

}

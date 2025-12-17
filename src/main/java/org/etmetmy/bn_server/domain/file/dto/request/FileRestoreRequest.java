package org.etmetmy.bn_server.domain.file.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileRestoreRequest {

    @NotNull(message = "파일 ID 목록은 필수입니다")
    @NotEmpty(message = "최소 1개 이상의 파일 ID가 필요합니다")
    @JsonProperty("file_ids")
    private List<Long> fileIds;

}

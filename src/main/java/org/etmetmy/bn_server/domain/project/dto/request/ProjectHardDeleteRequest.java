package org.etmetmy.bn_server.domain.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectHardDeleteRequest {

    @NotNull(message = "프로젝트 ID 목록은 필수입니다.")
    @NotEmpty(message = "최소 1개 이상의 프로젝트 ID가 필요합니다.")
    @JsonProperty("project_ids")
    private List<Long> projectIds;

}

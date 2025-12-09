package org.etmetmy.bn_server.domain.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRestoreRequest {

    @JsonProperty("project_ids")
    private List<Long> projectIds;
}

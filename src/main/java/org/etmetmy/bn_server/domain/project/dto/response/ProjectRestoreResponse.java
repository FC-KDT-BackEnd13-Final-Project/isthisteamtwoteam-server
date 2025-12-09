package org.etmetmy.bn_server.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRestoreResponse {

    @JsonProperty("restored_count")
    private Long restoredCount;

    @JsonProperty("restored_ids")
    private List<Long> restoredIds;

    public static class Converter {
        public static ProjectRestoreResponse from(List<Project> projects) {

            return ProjectRestoreResponse.builder()
                    .restoredCount((long)projects.size()).restoredIds(
                            projects.stream()
                                    .map(Project::getId).toList())
                    .build();
        }
    }
}

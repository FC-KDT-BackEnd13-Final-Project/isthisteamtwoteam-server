package org.etmetmy.bn_server.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectPermanentDeleteResponse {

    @JsonProperty("deleted_count")
    private Long deletedCount;

    @JsonProperty("deleted_ids")
    private List<Long> deletedIds;


    public static class Converter {
        public static ProjectPermanentDeleteResponse from(List<Project> projects) {

            return ProjectPermanentDeleteResponse.builder()
                    .deletedCount((long)projects.size())
                    .deletedIds(projects.stream().map(Project::getId).toList())
                    .build();
        }
    }
}

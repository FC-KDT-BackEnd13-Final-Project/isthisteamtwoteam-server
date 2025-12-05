package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStageUpdateResponse {
    private Long stageId;
    private Long updatedBy;

    public static class Converter {
        public static ProjectStageUpdateResponse of(Long stageId, Long updatedBy) {
            return ProjectStageUpdateResponse.builder()
                    .stageId(stageId)
                    .updatedBy(updatedBy)
                    .build();
        }
    }
}

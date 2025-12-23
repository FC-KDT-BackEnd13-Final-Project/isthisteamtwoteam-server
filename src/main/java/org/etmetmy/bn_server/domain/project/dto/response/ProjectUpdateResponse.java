package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.project.entity.Project;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectUpdateResponse {
    private Long projectId;
    private Long userId;


    public static class Converter{
        public static ProjectUpdateResponse from(Project project){
            return ProjectUpdateResponse.builder()
                    .projectId(project.getId())
                    .userId(project.getCreatedBy())
                    .build();
        }
    }

}

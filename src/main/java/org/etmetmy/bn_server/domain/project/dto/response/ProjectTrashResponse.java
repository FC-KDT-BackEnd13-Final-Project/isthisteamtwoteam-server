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
public class ProjectTrashResponse {
    private Long projectId;

    public static class Converter{
        public static ProjectTrashResponse from(Long projectId){
            return ProjectTrashResponse.builder()
                    .projectId(projectId)
                    .build();
        }
    }

}

package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.etmetmy.bn_server.domain.project.entity.Project;

@Getter
@AllArgsConstructor
public class ProjectCreateResponse {
    private Long projectId;

    public static class Converter {
        public static ProjectCreateResponse from(Project project) {
            return new ProjectCreateResponse(project.getId());
        }

        public static ProjectCreateResponse from(Long projectId) {
            return new ProjectCreateResponse(projectId);
        }
    }
}
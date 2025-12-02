package org.etmetmy.bn_server.domain.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectTitleUpdateRequest {
    private String projectName;
    private Long projectId;


    public static class Converter {
        // 필요시 변환 로직 추가 가능
        public static ProjectTitleUpdateRequest from(String projectName) {
            return new ProjectTitleUpdateRequest(projectName, null);
        }
    }

}

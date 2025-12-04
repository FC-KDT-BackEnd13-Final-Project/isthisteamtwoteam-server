package org.etmetmy.bn_server.domain.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectNameUpdateRequest {
    private String projectName;


    public static class Converter {
        // 필요시 변환 로직 추가 가능
        public static ProjectNameUpdateRequest from(String projectName) {
            return new ProjectNameUpdateRequest(projectName);
        }
    }

}

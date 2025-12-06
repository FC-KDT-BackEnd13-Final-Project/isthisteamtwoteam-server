package org.etmetmy.bn_server.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.project.entity.Project;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDetailResponse {

    @JsonProperty("projectId")
    private Long projectId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("coverImage")
    private CoverImage coverImage;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoverImage {
        @JsonProperty("fileName")
        private String fileName;

        @JsonProperty("url")
        private String url;
    }

    public static class Converter {
        public static ProjectDetailResponse from(Project project) {
            return ProjectDetailResponse.builder()
                    .projectId(project.getId())
                    .name(project.getProjectName())
                    .stage(project.getStage() != null ? project.getStage().getStageName() : null)
                    .coverImage(null) // TODO: 커버 이미지 구현 필요
                    .build();
        }
    }
}
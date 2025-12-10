package org.etmetmy.bn_server.domain.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectListResponse {

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_image_url")
    private String projectImageUrl;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("update_at")
    private LocalDateTime updateAt;

    @JsonProperty("has_permission")
    private boolean hasPermission;

    public static class Converter {
        public static List<ProjectListResponse> from(List<Project> projects, List<Long> myProjectIds) {
            return projects.stream()
                    .map(project -> ProjectListResponse.builder()
                            .projectId(project.getId())
                            .projectImageUrl(project.getProjectImageUrl())
                            .projectName(project.getProjectName())
                            .companyName(project.getCompany().getCompanyName())
                            .stage(project.getStage().getStageType().getDescription())
                            .startDate(project.getStartDate())
                            .endDate(project.getEndDate())
                            .updateAt(project.getUpdatedAt())
                            .hasPermission(myProjectIds.contains(project.getId()))
                            .build()
                    ).toList();
        }
    }
}

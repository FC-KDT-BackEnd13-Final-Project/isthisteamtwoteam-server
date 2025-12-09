package org.etmetmy.bn_server.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletedProjectResponse {

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_image_url")
    private String projectImageUrl;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("create_date")
    private LocalDate createDate;

    @JsonProperty("delete_date")
    private LocalDate deleteDate;

    public static class Converter {
        public static List<DeletedProjectResponse> from(List<Project> projects) {
            return projects.stream()
                    .map(project -> DeletedProjectResponse.builder()
                            .projectId(project.getId())
                            .projectImageUrl(project.getProjectImageUrl())
                            .projectName(project.getProjectName())
                            .companyName(project.getCompany().getCompanyName())
                            .createDate(LocalDate.from(project.getCreatedAt()))
                            .deleteDate(LocalDate.from(project.getDeletedAt()))
                            .build()
                    ).toList();
        }
    }
}


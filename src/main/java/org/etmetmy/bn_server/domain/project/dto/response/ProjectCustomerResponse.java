package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.*;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCustomerResponse {

    private Long projectId;
    private String projectName;
    private String projectImageUrl;
    private LocalDate startDate;
    private LocalDate endDate;

    private Long stageId;
    private String stageName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;

    public static class Converter {
        public static ProjectCustomerResponse from(Project project) {
            return ProjectCustomerResponse.builder()
                    .projectId(project.getId())
                    .projectName(project.getProjectName())
                    .projectImageUrl(project.getProjectImageUrl())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .stageId(project.getStage().getId())
                    .stageName(project.getStage().getStageName())
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .createdBy(project.getCreatedBy())
                    .build();
        }
        public static List<ProjectCustomerResponse> from(List<Project> projects) {
            return projects.stream()
                    .map(Converter::from)
                    .collect(Collectors.toList());
        }
    }
}
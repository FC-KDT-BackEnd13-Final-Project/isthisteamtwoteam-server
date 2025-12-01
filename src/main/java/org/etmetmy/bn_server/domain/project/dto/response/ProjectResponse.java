package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProjectResponse {
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;

    private Integer stageId;
    private String stageName;
    private String memoContent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;

    public static class Converter {
        public static ProjectResponse from(Project project) {
            return ProjectResponse.builder()
                    .projectId(project.getProjectId())
                    .projectName(project.getProjectName())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .stageId(project.getStage() != null ? project.getStage().getId() : null)
                    .stageName(project.getStage() != null ? project.getStage().getName() : null)
                    .memoContent(project.getMemo() != null ? project.getMemo().getContent() : null)
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .createdBy(project.getCreatedBy())
                    .build();
        }

        public static List<ProjectResponse> from(List<Project> projects) {
            return projects.stream()
                    .map(Converter::from)
                    .collect(Collectors.toList());
        }

    }


}

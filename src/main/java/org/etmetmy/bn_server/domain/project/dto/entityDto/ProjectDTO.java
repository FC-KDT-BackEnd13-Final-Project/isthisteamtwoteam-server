package org.etmetmy.bn_server.domain.project.dto.entityDto;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProjectDTO {
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long companyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;

    public static class Converter {

        public static ProjectResponse toResponse(Project project){
            return ProjectResponse.builder()
                    .projectId(project.getProjectId())
                    .projectName(project.getProjectName())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .stageId(project.getStageId() != null ? project.getStage().getStageId().intValue() : null)
                    .stageName(project.getStageName() != null ? project.getStage().getName() : null)
                    .memoContent(project.getMemoContent() != null ? project.getMemo().getContent() : null)
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .createdBy(project.getCreatedBy())
                    .build();
        }

        public static List<ProjectResponse> toResponseList(List<Project> projects){
            return projects.stream()
                    .map(Converter::toResponse)
                    .collect(Collectors.toList());
        }

    }
}

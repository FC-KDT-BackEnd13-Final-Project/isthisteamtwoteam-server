package org.etmetmy.bn_server.domain.project.dto.entity;

import lombok.*;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.user.entity.ProjectMember;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
            Memo memo = (project.getMemo() != null && !project.getMemo().isEmpty())
                    ? project.getMemo().get(0)
                    : null;

            return ProjectResponse.builder()
                    .projectId(project.getId())
                    .projectName(project.getProjectName())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .stageId(project.getStage() != null ? project.getStage().getStageId().intValue() : null)
                    .stageName(project.getStage() != null ? project.getStage().getStageName() : null)
                    .memoContent(memo != null ? memo.getContent() : null)
                    .members(null)
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .createdBy(project.getCreatedBy())
                    .build();
        }

        public static ProjectResponse toResponse(Project project, List<ProjectMember> members){
            Memo memo = (project.getMemo() != null && !project.getMemo().isEmpty())
                    ? project.getMemo().get(0)
                    : null;

            return ProjectResponse.builder()
                    .projectId(project.getId())
                    .projectName(project.getProjectName())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .stageId(project.getStage() != null ? project.getStage().getStageId().intValue() : null)
                    .stageName(project.getStage() != null ? project.getStage().getStageName() : null)
                    .memoContent(memo != null ? memo.getContent() : null)
                    .members(org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse.Converter.from(members))
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

        public static List<ProjectResponse> toResponseList(List<Project> projects, Map<Long, List<ProjectMember>> membersByProjectId){
            return projects.stream()
                    .map(project -> Converter.toResponse(project, membersByProjectId.getOrDefault(project.getId(), List.of())))
                    .collect(Collectors.toList());
        }
    }

}

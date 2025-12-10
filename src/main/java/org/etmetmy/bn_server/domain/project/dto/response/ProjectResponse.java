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
public class ProjectResponse {

    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;

    private Integer stageId;
    private String stageName;
    private String memoContent;
    private List<Long> members;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;

    public static class Converter {
        public static ProjectResponse from(Project project,  List<ProjectMember> members) {
            Memo memo = (project.getMemo() != null && !project.getMemo().isEmpty())
                    ? project.getMemo().get(0)
                    : null;

            List<Long> memberUserIds = members == null ? List.of()
                    : members.stream()
                    .map(pm -> pm.getUser() != null ? pm.getUser().getId() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ProjectResponse.builder()
                    .projectId(project.getId())
                    .projectName(project.getProjectName())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .stageId(project.getStage() != null ? project.getStage().getId().intValue() : null)
                    .stageName(project.getStage().getStageType().getDescription())
                    .memoContent(memo != null ? memo.getContent() : null)
                    .members(memberUserIds)
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .createdBy(project.getCreatedBy())
                    .build();
        }

        public static List<ProjectResponse> from(List<Project> projects, Map<Long, List<ProjectMember>> membersByProjectId) {
            return projects.stream()
                    .map(project -> Converter.from(project, membersByProjectId.getOrDefault(project.getId(), List.of())))
                    .collect(Collectors.toList());
        }
    }
}
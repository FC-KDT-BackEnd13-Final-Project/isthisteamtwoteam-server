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
    private String projectImageUrl;
    private LocalDate startDate;
    private LocalDate endDate;

    private Integer stageId;
    private String stageName;
    private String memoContent;
    private List<Long> members;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Boolean isAttend;

    public static class Converter {
        public static ProjectResponse from(Project project,  List<ProjectMember> members) {
            return from(project, members, null);
        }

        public static ProjectResponse from(Project project, List<ProjectMember> members, Long loginUserId) {
            Memo memo = (project.getMemos() != null && !project.getMemos().isEmpty())
                    ? project.getMemos().getFirst()
                    : null;

            List<Long> memberUserIds = members == null ? List.of()
                    : members.stream()
                    .map(pm -> pm.getUser() != null ? pm.getUser().getId() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            boolean isAttend = false;
            if (loginUserId != null && members != null) {
                isAttend = members.stream()
                        .anyMatch(pm -> pm.getUser() != null && pm.getUser().getId().equals(loginUserId));
            }

            return ProjectResponse.builder()
                    .projectId(project.getId())
                    .projectName(project.getProjectName())
                    .projectImageUrl(project.getProjectImageUrl())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .stageId(project.getStage() != null ? project.getStage().getId().intValue() : null)
                    .stageName(project.getStage() != null ? project.getStage().getStageName() : null)
                    .memoContent(memo != null ? memo.getContent() : null)
                    .members(memberUserIds)
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .createdBy(project.getCreatedBy())
                    .isAttend(isAttend)
                    .build();
        }

        public static List<ProjectResponse> from(List<Project> projects, Map<Long, List<ProjectMember>> membersByProjectId) {
            return from(projects, membersByProjectId, null);
        }

        public static List<ProjectResponse> from(List<Project> projects, Map<Long, List<ProjectMember>> membersByProjectId, Long loginUserId) {
            return projects.stream()
                    .map(project -> Converter.from(project, membersByProjectId.getOrDefault(project.getId(), List.of()), loginUserId))
                    .collect(Collectors.toList());
        }
    }
}
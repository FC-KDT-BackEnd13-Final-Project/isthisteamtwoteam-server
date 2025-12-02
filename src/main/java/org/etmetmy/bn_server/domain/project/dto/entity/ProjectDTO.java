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
import java.util.Objects;
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

        public static ProjectResponse toResponse(Project project, List<ProjectMember> members){
            Memo memo = (project.getMemo() != null && !project.getMemo().isEmpty())
                    ? project.getMemo().get(0)
                    : null;

            // ProjectMember에서 userId만 추출하여 List<Long>으로 변환
            List<Long> memberUserIds = members.stream()
                    .map(pm -> pm.getUser() != null ? pm.getUser().getId() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ProjectResponse.builder()
                    .projectId(project.getId())
                    .projectName(project.getProjectName())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .stageId(project.getStage() != null ? project.getStage().getStageId().intValue() : null)
                    .stageName(project.getStage() != null ? project.getStage().getStageName() : null)
                    .memoContent(memo != null ? memo.getContent() : null)
                    .members(memberUserIds)  // userId 리스트로 설정
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .createdBy(project.getCreatedBy())
                    .build();
        }

        public static List<ProjectResponse> toResponseList(List<Project> projects, Map<Long, List<ProjectMember>> membersByProjectId){
            return projects.stream()
                    .map(project -> {
                        List<ProjectMember> members = membersByProjectId.getOrDefault(project.getId(), List.of());
                        return Converter.toResponse(project, members);
                            })
                    .collect(Collectors.toList());
        }
    }
}
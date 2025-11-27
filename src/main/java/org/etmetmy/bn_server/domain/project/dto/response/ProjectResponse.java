package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.Builder;
import lombok.Getter;
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

    private Integer stageId;               // TODO: 단계 도메인 생기면 매핑
    private String content;                // TODO: 메모 도메인/필드 추가 시 사용

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
                    .stageId(null)         // 아직 Project 엔티티에 단계 필드 없음
                    .content(null)         // 아직 엔티티에 없음
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

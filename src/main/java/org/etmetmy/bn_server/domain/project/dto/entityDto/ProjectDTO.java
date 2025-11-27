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
                    .stageId(null)              // 추후 단계 정보 매핑
                    .content(null)              // 추후 메모 매핑
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .createdBy(project.getCreatedBy())
                    .build();
        }
//        public static ProjectDTO from(Project project) {
//            if (project == null) {
//                return null;
//            }
//
//            return ProjectDTO.builder()
//                    .projectId(project.getProjectId())
//                    .projectName(project.getProjectName())
//                    .startDate(project.getStartDate())
//                    .endDate(project.getEndDate())
//                    .companyId(project.getCompanyEntity() != null ? project.getCompanyEntity().getCompanyId() : null)
//                    .createdAt(project.getCreatedAt())
//                    .updatedAt(project.getUpdatedAt())
//                    .createdBy(project.getCreatedBy())
//                    .build();
//        }

        public static List<ProjectResponse> toResponseList(List<Project> projects){
            return projects.stream()
                    .map(Converter::toResponse)
                    .collect(Collectors.toList());
        }

    }
}

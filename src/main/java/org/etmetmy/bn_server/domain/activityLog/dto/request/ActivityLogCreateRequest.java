package org.etmetmy.bn_server.domain.activityLog.dto.request;

import lombok.Builder;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.activityLog.util.ActivityDescriptionGenerator;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;

@Builder
public record ActivityLogCreateRequest(
        Long projectId,
        String projectName,
        Long userId,
        String userName,
        ActivityAction action,
        String targetType,
        Long targetId,
        String ipAddress,
        String description
) {
    public static class Converter {

        public static ActivityLog toEntity(ActivityLogCreateRequest request) {
            return ActivityLog.builder()
                    .projectId(request.projectId())
                    .projectName(request.projectName())
                    .userId(request.userId())
                    .userName(request.userName())
                    .action(request.action())
                    .targetType(request.targetType())
                    .targetId(request.targetId())
                    .ipAddress(request.ipAddress())
                    .description(request.description())
                    .build();
        }


        public static ActivityLog toEntityWithDetail(
                ActivityLogCreateRequest request,
                String description,
                User user,
                Project project
        ) {
            return ActivityLog.builder()
                    .projectId(request.projectId())
                    .projectName(project != null ? project.getProjectName() : "알 수 없는 프로젝트")
                    .userId(request.userId())
                    .userName(user != null ? user.getName() : "알 수 없는 사용자")
                    .action(request.action())
                    .targetType(request.targetType())
                    .targetId(request.targetId())
                    .ipAddress(request.ipAddress())
                    .description(description)
                    .build();
        }


        public static ActivityLog toEntityWithRepositories(
                ActivityLogCreateRequest request,
                UserRepository userRepository,
                ProjectRepository projectRepository,
                ActivityDescriptionGenerator descriptionGenerator
        ) {
            // 1. 필요한 엔티티 조회
            User user = userRepository.findById(request.userId()).orElse(null);
            Project project = projectRepository.findById(request.projectId()).orElse(null);

            // 2. 한글 설명 생성
            String description = descriptionGenerator.generate(
                    request.action(),
                    request.targetType(),
                    request.targetId(),
                    user,
                    project
            );

            // 3. Entity 생성
            return toEntityWithDetail(request, description, user, project);
        }
    }
}
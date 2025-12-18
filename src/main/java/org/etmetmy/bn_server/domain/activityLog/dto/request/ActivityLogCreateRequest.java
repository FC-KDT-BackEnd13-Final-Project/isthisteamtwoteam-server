package org.etmetmy.bn_server.domain.activityLog.dto.request;

import lombok.Builder;
import org.etmetmy.bn_server.domain.activityLog.entity.ActivityLog;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.user.entity.User;

@Builder
public record ActivityLogCreateRequest(
        Long projectId,
        Long userId,
        ActivityAction action,
        String targetType,
        Long targetId,
        String ipAddress,
        String detail
) {
    public static class Converter {

        /**
         * User, Project, Description을 모두 받아서 완전한 ActivityLog 엔티티 생성
         */
        public static ActivityLog toEntity(
                ActivityLogCreateRequest request,
                User user,
                Project project,
                String description
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
                    .detail(request.detail())
                    .build();
        }
    }
}
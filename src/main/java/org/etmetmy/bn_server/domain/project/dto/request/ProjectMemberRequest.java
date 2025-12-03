package org.etmetmy.bn_server.domain.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.project.entity.Project;

import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.etmetmy.bn_server.domain.user.entity.User;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberRequest {
    private Long userId;

    public static class Converter{
        public static ProjectMember toEntity(ProjectMemberRequest request, Project project, User user, Long createdById){
            return ProjectMember.builder()
                    .project(project)
                    .user(user)
                    .assignedBy(createdById)
                    .build();
        }

    }
}

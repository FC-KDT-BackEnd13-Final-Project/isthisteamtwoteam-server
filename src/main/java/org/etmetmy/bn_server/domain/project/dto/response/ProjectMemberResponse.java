package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.*;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;


import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberResponse {
    private Long projectMemberId;
    private Long userId;
    private String userName;

    public static class Converter{
        public static ProjectMemberResponse from(ProjectMember projectMember){
            return ProjectMemberResponse.builder()
                    .projectMemberId(projectMember.getProjectMemberId())
                    .userId(projectMember.getUser() != null ? projectMember.getUser().getId() : null)
                    .userName(projectMember.getUser() != null ? projectMember.getUser().getName() : null)
                    .build();
        }

        public static List<ProjectMemberResponse> from(List<ProjectMember> members) {
            return members.stream()
                    .map(Converter::from)
                    .collect(Collectors.toList());
        }
    }

}

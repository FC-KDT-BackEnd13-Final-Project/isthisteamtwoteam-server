package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectMemberSearchResponse {
    private Long userId;
    private String userName;
    private String email;

    public static class Converter{
        public static ProjectMemberSearchResponse from(Long userId, String userName, String email){
            return ProjectMemberSearchResponse.builder()
                    .userId(userId)
                    .userName(userName)
                    .email(email)
                    .build();
        }
    }
}

package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectMemberSearchResponse {
    private Long userId;
    private String userName;
    private String email;
    private Long companyId;
    private String companyName;

    public static class Converter{
        public static ProjectMemberSearchResponse from(Long userId, String userName, String email, Long companyId, String companyName){
            return ProjectMemberSearchResponse.builder()
                    .userId(userId)
                    .userName(userName)
                    .email(email)
                    .companyId(companyId)
                    .companyName(companyName)
                    .build();
        }
        // 새로운 메서드 (List<User>를 받아서 변환)
        public static List<ProjectMemberSearchResponse> from(List<User> users, Role role) {
            return users.stream()
                    .map(user -> ProjectMemberSearchResponse.builder()
                            .userId(user.getId())
                            .userName(user.getName())
                            .email(user.getEmail())
                            .companyId(user.getCompany() != null ? user.getCompany().getCompanyId() : null)
                            .companyName(role == Role.DEVELOPER ? null :
                                    (user.getCompany() != null ? user.getCompany().getCompanyName() : null))
                            .build())
                    .toList();
        }
    }
}

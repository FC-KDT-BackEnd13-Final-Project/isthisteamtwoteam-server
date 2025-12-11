package org.etmetmy.bn_server.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
        private static ProjectMemberSearchResponse from(User user, Role role) {
            return ProjectMemberSearchResponse.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .email(user.getEmail())
                    .companyId(user.getCompany() != null ? user.getCompany().getCompanyId() : null)
                    .companyName(
                            role == Role.CUSTOMER && user.getCompany() != null
                                    ? user.getCompany().getCompanyName()
                                    : null   // DEVELOPER 이면 항상 null
                    )
                    .build();
        }
        public static List<ProjectMemberSearchResponse> from(List<User> users, Role role) {
            return users.stream()
                    .map(user -> from(user, role))
                    .toList();
        }
        // 이미 등록된 사용자를 제외하고 반환
        public static List<ProjectMemberSearchResponse> filteredUsers(List<User> users, Role role, Set<Long> excludeUserIds) {
            return users.stream()
                    .filter(user -> !excludeUserIds.contains(user.getId()))
                    .map(user -> from(user, role))
                    .toList();
        }
    }
}

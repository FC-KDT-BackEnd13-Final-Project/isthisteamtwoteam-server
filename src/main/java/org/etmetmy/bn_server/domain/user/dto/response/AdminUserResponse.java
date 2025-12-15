package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class AdminUserResponse {

    private final Long userId;
    private final String name;
    private final String email;
    private final String phone;
    private final String companyName;

    public static class Converter {

        public static AdminUserResponse from(User user) {
            return AdminUserResponse.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .companyName(user.getCompany().getCompanyName())
                    .build();
        }


        public static List<AdminUserResponse> fromList(List<User> users) {
            return users.stream()
                    .filter(user -> user.getRole() == Role.ADMIN)
                    .map(Converter::from)
                    .toList();
        }
    }
}

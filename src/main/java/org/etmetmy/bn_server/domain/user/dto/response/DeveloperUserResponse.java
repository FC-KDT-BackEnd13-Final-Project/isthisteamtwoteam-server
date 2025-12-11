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
public class DeveloperUserResponse {
    private final String name;
    private final String email;
    private final String phone;


    public static class Converter {

        public static DeveloperUserResponse from(User user) {
            return DeveloperUserResponse.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .build();
        }


        public static List<DeveloperUserResponse> fromList(List<User> users) {
            return users.stream()
                    .filter(user -> user.getRole() == Role.DEVELOPER)
                    .map(Converter::from)
                    .toList();
        }
    }
}
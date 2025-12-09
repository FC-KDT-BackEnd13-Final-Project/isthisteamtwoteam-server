package org.etmetmy.bn_server.domain.user.dto.response;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.User;

@Getter
@RequiredArgsConstructor
public class DeveloperUserResponse {
    private final String name;
    private final String email;
    private final String phone;


    public DeveloperUserResponse(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();

    }
}
package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordFindResponse {

    private String email;
    private int expiresInSeconds;

    public static PasswordFindResponse from(String email, int expiresInSeconds) {
        return new PasswordFindResponse(email, expiresInSeconds);
    }
}

package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetResponse {

    private Long userId;

    public static class Converter {
        public static PasswordResetResponse from(Long userId) {
            return new PasswordResetResponse(userId);
        }
    }
}

package org.etmetmy.bn_server.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetVerifyCodeRequest {
    private String email;
    private String code;
    private String newPassword;
}

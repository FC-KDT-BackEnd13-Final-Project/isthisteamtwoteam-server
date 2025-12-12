package org.etmetmy.bn_server.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}

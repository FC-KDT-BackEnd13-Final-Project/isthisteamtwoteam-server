package org.etmetmy.bn_server.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetVerifyCodeRequest {
    @NotBlank(message = "이메일은 필수입니다")
    private String email;

    @NotBlank(message = "인증 코드는 필수입니다")
    private String code;

    @NotBlank(message = "새 비밀번호는 필수입니다")
    private String newPassword;
}

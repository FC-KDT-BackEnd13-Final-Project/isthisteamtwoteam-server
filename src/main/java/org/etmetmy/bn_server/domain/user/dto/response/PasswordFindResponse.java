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
    //private String code; //테스트용 - 인증번호 12345

    public static PasswordFindResponse of(String email, int expiresInSeconds) {
        return PasswordFindResponse.builder()
                .email(email)
                .expiresInSeconds(expiresInSeconds)
                .build();
    }
}

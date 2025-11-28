package org.etmetmy.bn_server.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {
    private String name;
    private String email;
    private Long companyId;
    private String role;    // "ADMIN" or "USER"
}
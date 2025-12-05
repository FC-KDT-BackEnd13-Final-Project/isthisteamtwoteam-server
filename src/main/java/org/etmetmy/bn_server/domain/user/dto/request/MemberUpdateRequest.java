package org.etmetmy.bn_server.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.Role;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequest {
    private String name;
    private String email;
    private Long companyId;
    private Role role;    // "ADMIN" or "USER"
}
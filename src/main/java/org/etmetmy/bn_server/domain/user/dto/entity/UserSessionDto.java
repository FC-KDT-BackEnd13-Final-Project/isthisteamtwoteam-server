package org.etmetmy.bn_server.domain.user.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.etmetmy.bn_server.domain.user.entity.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionDto {
    private Long id;
    private String name;
    private Role role;
}
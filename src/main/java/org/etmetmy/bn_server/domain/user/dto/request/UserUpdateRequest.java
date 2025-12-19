package org.etmetmy.bn_server.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.Role;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String name;
    private String email;
    private String password;

    @JsonProperty("profile_img")
    private String profileImg;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("company_name")
    private String companyName;

    private Role role;
}
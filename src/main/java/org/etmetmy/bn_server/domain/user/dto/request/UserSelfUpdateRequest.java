package org.etmetmy.bn_server.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserSelfUpdateRequest {
    private String name;

    private String password;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("company_name")
    private String companyName;
}

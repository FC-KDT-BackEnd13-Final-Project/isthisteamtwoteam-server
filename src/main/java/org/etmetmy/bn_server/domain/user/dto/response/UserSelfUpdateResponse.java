package org.etmetmy.bn_server.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserSelfUpdateResponse {
    @JsonProperty("user_id")
    private Long userId;

    private String name;

    private String phone;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static class Converter {
        public static UserSelfUpdateResponse from(User user) {
            return UserSelfUpdateResponse.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .phone(user.getPhone())
                    .companyName(user.getCompany().getCompanyName())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }
}

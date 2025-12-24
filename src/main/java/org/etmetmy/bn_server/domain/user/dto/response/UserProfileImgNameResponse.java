package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.User;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserProfileImgNameResponse {
    private String profileImg;
    private String name;
    private String role;

    public static class Converter {

        public static UserProfileImgNameResponse from(User user) {
            return UserProfileImgNameResponse.builder()
                    .profileImg(user.getProfileImg())
                    .name(user.getName())
                    .role(user.getRole().getDescription())
                    .build();
        }
    }
}

package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserProfileImgNameResponse {
    private String profileImg;
    private String name;

    public static class Converter{
        public static UserProfileImgNameResponse from(String profileImg, String name ){
            return UserProfileImgNameResponse.builder()
                    .profileImg(profileImg)
                    .name(name)
                    .build();
        }
    }
}

package org.etmetmy.bn_server.domain.user.dto.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.Company;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;

@Builder
@Getter
@AllArgsConstructor

public class UserDto {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String company;

    @Enumerated(EnumType.STRING)
    private Role role;



    public static class Converter{
        //todo : userDto -> user 로 변환하는 컨버터
        public static User toUser(UserDto userDto, Company company){
            return User.builder()
                    .name(userDto.getName())
                    .email(userDto.getEmail())
                    .phone(userDto.getPhone())
                    .company(company)
                    .role(userDto.getRole())
                    .build();
        }
    }
}

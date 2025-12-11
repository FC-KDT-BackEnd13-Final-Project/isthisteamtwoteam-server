package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class CustomerUserResponse {

    private final String name;
    private final String email;
    private final String phone;
    private final String companyName;

    public static class Converter {

        public static CustomerUserResponse from(User user) {
            // DTO의 Builder를 사용하여 객체 생성
            return CustomerUserResponse.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .companyName(user.getCompany().getCompanyName())
                    .build();
        }


        public static List<CustomerUserResponse> fromList(List<User> users) {
            return users.stream()
                    .filter(user -> user.getRole() == Role.CUSTOMER)
                    .map(Converter::from)
                    .toList();
        }
    }
}

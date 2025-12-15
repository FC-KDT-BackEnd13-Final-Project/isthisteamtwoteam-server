package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@Builder
@RequiredArgsConstructor
public class UserDataResponse {

    private final UserItems<AdminUserResponse> admins;
    private final UserItems<DeveloperUserResponse> developers;
    private final UserItems<CustomerUserResponse> customers;
    private final UserItems<CompanySearchResponse> companies;

    public static class Converter {
        // DTO 조립을 위한 정적 메서드
        public static UserDataResponse createResponse(
                UserItems<AdminUserResponse> adminItems,
                UserItems<DeveloperUserResponse> developerItems,
                UserItems<CustomerUserResponse> customerItems,
                UserItems<CompanySearchResponse> companyItems) {

            // DTO의 Builder를 사용하여 new 키워드를 사용하지 않음
            return UserDataResponse.builder()
                    .admins(adminItems)
                    .developers(developerItems)
                    .customers(customerItems)
                    .companies(companyItems)
                    .build();
        }
    }
}
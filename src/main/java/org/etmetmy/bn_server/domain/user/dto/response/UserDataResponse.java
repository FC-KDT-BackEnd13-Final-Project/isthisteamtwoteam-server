package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@Builder
@RequiredArgsConstructor
public class UserDataResponse {

    private final UserItems<DeveloperUserResponse> developers;
    private final UserItems<CustomerUserResponse> customers;
    private final UserItems<CompanySearchResponse> companies;

    public static UserDataResponse create(
            UserItems<DeveloperUserResponse> developerItems,
            UserItems<CustomerUserResponse> customerItems,
            UserItems<CompanySearchResponse> companyItems) {

        return UserDataResponse.builder()
                .developers(developerItems)
                .customers(customerItems)
                .companies(companyItems)
                .build(); // new 대신 Builder 사용
    }
}
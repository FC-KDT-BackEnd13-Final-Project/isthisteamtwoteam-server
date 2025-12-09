package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class UserDataResponse {

    // DeveloperMemberResponse를 담는 목록
    private final UserItems<DeveloperUserResponse> developers;
    // ClientMemberResponse를 담는 목록
    private final UserItems<CustomerUserResponse> customers;
    // CompanySearchResponse를 담는 목록
    private final UserItems<CompanySearchResponse> companies;
}
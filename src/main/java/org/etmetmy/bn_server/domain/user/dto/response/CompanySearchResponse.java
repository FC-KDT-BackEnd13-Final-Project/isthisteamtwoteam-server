package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;

@Getter
@RequiredArgsConstructor
public class CompanySearchResponse {
    private final Long id;
    private final String companyName;
    private final String address;
    private final String manager;
    private final String userPhone;


    public CompanySearchResponse(Company company) {
        this.id = company.getCompanyId();
        this.companyName = company.getCompanyName();
        this.address = company.getCompanyAddress();
        this.manager = company.getCompanyContactPerson();
        this.userPhone = company.getCompanyContactPhone();
    }
}
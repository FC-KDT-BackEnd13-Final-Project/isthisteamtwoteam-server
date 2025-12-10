package org.etmetmy.bn_server.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class CompanySearchResponse {
    private final Long id;
    private final String companyName;
    private final String address;
    private final String manager;
    private final String userPhone;


    public static class Converter {

        public static CompanySearchResponse from(Company company) {

            return CompanySearchResponse.builder()
                    .id(company.getCompanyId())
                    .companyName(company.getCompanyName())
                    .address(company.getCompanyAddress())
                    .manager(company.getCompanyContactPerson())
                    .userPhone(company.getCompanyContactPhone())
                    .build();
        }

        public static List<CompanySearchResponse> fromList(List<Company> companies) {
            return companies.stream()
                    .map(Converter::from)
                    .toList();
        }
    }
}
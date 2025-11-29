package org.etmetmy.bn_server.domain.company.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.etmetmy.bn_server.domain.company.entity.Company;

@Getter
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;

    // 엔티티 -> DTO 변환 메서드
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(company.getCompanyId(), company.getCompanyName());
    }
}
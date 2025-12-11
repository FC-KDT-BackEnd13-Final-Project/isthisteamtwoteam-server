package org.etmetmy.bn_server.domain.company.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.dashboard.dto.response.ProjectListResponse;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CompanyResponse {
    private Long id;
    private String name;

    // 엔티티 -> DTO 변환 메서드
    public static class Converter {
        public static CompanyResponse from(Company company) {
            return CompanyResponse.builder()
                    .id(company.getCompanyId())
                    .name(company.getCompanyName())
                    .build();
        }

        public static List<CompanyResponse> from(List<Company> companies) {
            return companies.stream()
                    .map(company -> CompanyResponse.builder()
                            .id(company.getCompanyId())
                            .name(company.getCompanyName())
                            .build()
                    ).toList();
        }
    }
}
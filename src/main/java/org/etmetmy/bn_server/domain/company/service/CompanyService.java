package org.etmetmy.bn_server.domain.company.service;

import org.etmetmy.bn_server.domain.company.dto.request.CompanyCreateRequest;
import org.etmetmy.bn_server.domain.company.dto.response.CompanyResponse;
import org.etmetmy.bn_server.domain.company.entity.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyService {
    Long createCompany(CompanyCreateRequest request);

    List<CompanyResponse> getAllCompanies();

    Optional<Company> findByCompanyName(String companyName);
}

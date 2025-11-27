package org.etmetmy.bn_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.entity.Company;
import org.etmetmy.bn_server.domain.user.repository.CompanyRepository;
import org.etmetmy.bn_server.global.StatusCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService{
    private final CompanyRepository companyRepository;
    @Override
    public Company findByCompanyName(String companyName) {
        Company company = companyRepository.findByCompanyName(companyName);

        return company;
    }
}

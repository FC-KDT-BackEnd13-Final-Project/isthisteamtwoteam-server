package org.etmetmy.bn_server.domain.company.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.dto.request.CompanyCreateRequest;
import org.etmetmy.bn_server.domain.company.dto.response.CompanyResponse;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public Long createCompany(CompanyCreateRequest request) {
        // 1. DTO를 Entity로 변환
        Company company = CompanyCreateRequest.Converter.toEntity(request);

        // 2. 저장
        Company savedCompany = companyRepository.save(company);

        // 3. 생성된 ID 반환
        return savedCompany.getCompanyId();
    }

    // 전체 회사 이름 목록 조회
    public List<CompanyResponse> getAllCompanyNames() {

        List<Company> companies = companyRepository.findAll().stream().toList();
        return CompanyResponse.Converter.from(companies);
    }

    @Override
    public Optional<Company> findByCompanyName(String companyName) {
        return companyRepository.findByCompanyName(companyName);
    }
}
package org.etmetmy.bn_server.domain.company.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.dto.CompanyCreateRequest;
import org.etmetmy.bn_server.domain.company.dto.CompanyResponse;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public Long createCompany(CompanyCreateRequest request) {
        // 1. DTO를 Entity로 변환
        Company company = request.toEntity();

        // 2. 저장
        Company savedCompany = companyRepository.save(company);

        // 3. 생성된 ID 반환
        return savedCompany.getCompanyId();
    }

    // 전체 회사 목록 조회
    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(CompanyResponse::from) // 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }
}
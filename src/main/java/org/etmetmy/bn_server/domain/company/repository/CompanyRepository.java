package org.etmetmy.bn_server.domain.company.repository;

import org.etmetmy.bn_server.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    // 필요 시 중복 검사 메서드 추가 (예: boolean existsByCompanyName(String name);)
    Optional<Company> findByCompanyName(String companyName);

}
package org.etmetmy.bn_server.domain.user.service;

import org.etmetmy.bn_server.domain.user.entity.Company;

import java.util.Optional;

public interface CompanyService {

    Optional<Company> findByCompanyName(String companyName);
}

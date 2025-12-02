package org.etmetmy.bn_server.domain.service;


import org.etmetmy.bn_server.domain.company.entity.Company;

import java.util.Optional;

public interface CompanyService {

    Optional<Company> findByCompanyName(String companyName);
}

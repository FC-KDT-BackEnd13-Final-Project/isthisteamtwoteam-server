package org.etmetmy.bn_server.domain.user.service;

import org.etmetmy.bn_server.domain.user.entity.Company;

public interface CompanyService {

    Company findByCompanyName(String companyName);
}

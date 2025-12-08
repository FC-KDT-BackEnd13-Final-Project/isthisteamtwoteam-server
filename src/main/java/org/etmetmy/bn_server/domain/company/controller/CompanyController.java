package org.etmetmy.bn_server.domain.company.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.dto.request.CompanyCreateRequest;
import org.etmetmy.bn_server.domain.company.dto.response.CompanyResponse;
import org.etmetmy.bn_server.domain.company.service.CompanyService;
import org.etmetmy.bn_server.domain.company.service.CompanyServiceImpl;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // 회사 생성 API
    @PostMapping
    public CommonResponse<Long> createCompany(@RequestBody @Valid CompanyCreateRequest request) {
        return CommonResponse.success("회사 생성 완료",companyService.createCompany(request));
    }

    //회사 목록 조회 API
    @GetMapping
    public CommonResponse<List<CompanyResponse>> getAllCompanies() {
        return CommonResponse.success("회사 전체 조회 완료",companyService.getAllCompanies());
    }
}
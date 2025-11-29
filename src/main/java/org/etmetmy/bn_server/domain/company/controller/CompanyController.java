package org.etmetmy.bn_server.domain.company.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.dto.CompanyCreateRequest;
import org.etmetmy.bn_server.domain.company.dto.CompanyResponse;
import org.etmetmy.bn_server.domain.company.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // 회사 생성 API
    @PostMapping
    public ResponseEntity<String> createCompany(@RequestBody @Valid CompanyCreateRequest request) {
        Long companyId = companyService.createCompany(request);
        return ResponseEntity.ok("회사 생성 완료. ID: " + companyId);
    }

    //회사 목록 조회 API
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        List<CompanyResponse> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }
}
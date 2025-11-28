package org.etmetmy.bn_server.domain.company.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;

@Getter
@NoArgsConstructor
public class CompanyCreateRequest {

    @NotBlank(message = "회사명은 필수입니다.")
    private String companyName;

    private String companyAddress;
    private String companyCeo;

    @NotBlank(message = "회사 전화번호는 필수입니다.")
    private String companyPhone; // 회사 대표 번호

    @NotBlank(message = "담당자명은 필수입니다.")
    private String companyContactPerson; // 담당자 이름

    private String companyContactPhone; // 담당자 연락처 (선택으로 둠, 필요시 @NotBlank 추가)

    private String businessRegistration; // 사업자등록번호 (String)

    // DTO -> Entity 변환
    public Company toEntity() {
        return Company.builder()
                .companyName(this.companyName)
                .companyAddress(this.companyAddress)
                .companyCeo(this.companyCeo)
                .companyPhone(this.companyPhone)
                .companyContactPerson(this.companyContactPerson)
                .companyContactPhone(this.companyContactPhone)
                .businessRegistration(this.businessRegistration)
                .build();
    }
}
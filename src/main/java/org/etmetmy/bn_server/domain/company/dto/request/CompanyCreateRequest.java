package org.etmetmy.bn_server.domain.company.dto.request;

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

    public static class Converter{
        // DTO -> Entity 변환
        public static Company toEntity(CompanyCreateRequest request) {
            return Company.builder()
                    .companyName(request.getCompanyName())
                    .companyAddress(request.getCompanyAddress())
                    .companyCeo(request.getCompanyCeo())
                    .companyPhone(request.getCompanyPhone())
                    .companyContactPerson(request.getCompanyContactPerson())
                    .companyContactPhone(request.getCompanyContactPhone())
                    .businessRegistration(request.getBusinessRegistration())
                    .build();
        }
    }

}
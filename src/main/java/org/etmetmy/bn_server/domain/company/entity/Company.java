package org.etmetmy.bn_server.domain.company.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.global.entity.BaseEntity;

@Entity
@Table(name = "company")
@Getter
@NoArgsConstructor
@AllArgsConstructor

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@SuperBuilder
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "company_address", length = 500)
    private String companyAddress;

    @Column(name = "company_ceo", length = 100)
    private String companyCeo;

    @Column(name = "company_phone", length = 20)
    private String companyPhone;

    @Column(name = "company_contact_person", length = 100)
    private String companyContactPerson;

    @Column(name = "company_contact_phone", length = 20)
    private String companyContactPhone;

    @Column(name = "business_registration", length = 500)
    private String businessRegistration;

    @Enumerated(EnumType.STRING) // DB에 "DEVELOPER", "CLIENT" 로 저장됨
    @Column(name = "company_type")
    private CompanyType type;
}

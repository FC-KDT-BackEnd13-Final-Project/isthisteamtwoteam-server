package org.etmetmy.bn_server.domain.company.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "company")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Company {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING) // DB에 "DEVELOPER", "CLIENT" 로 저장됨
    @Column(name = "company_type")
    private CompanyType type;
}

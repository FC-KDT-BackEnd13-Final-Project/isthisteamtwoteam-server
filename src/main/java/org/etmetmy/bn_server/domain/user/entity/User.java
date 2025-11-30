package org.etmetmy.bn_server.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.global.entity.BaseEntity;

@Entity
@Table(name = "\"User\"") // PostgreSQL의 예약어 처리
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "role", nullable = false, length = 255)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "phone", length = 20)
    private String phone;

    public void updateInfo(String name, String email, Company company, Role role) {
        this.name = name;
        this.email = email;
        this.company = company; // 회사 이동 가능
        this.role = role;       // 관리자 권한 부여 가능
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

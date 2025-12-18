package org.etmetmy.bn_server.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.global.entity.BaseEntity;
import java.io.Serial;

@Entity
@Table(name = "\"User\"") // PostgreSQL의 예약어 처리
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity implements java.io.Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "role", nullable = false, length = 255)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "profile_img")
    private String profileImg;

    public void updateInfo(String name, String email, String phone, Company company, Role role) {
        if (name != null) {
            this.name = name;
        }
        if (email != null) {
            this.email = email;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (company != null) {
            this.company = company; // 회사 이동 가능
        }
        if (role != null) {
            this.role = role;       // 관리자 권한 부여 가능
        }
    }

    public void updatePassword(String password) {
        this.password = password;
    }
    public void updateProfileImage(String ImageUrl){
        this.profileImg = ImageUrl;
    }
}

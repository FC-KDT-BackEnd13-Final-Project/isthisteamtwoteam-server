package org.etmetmy.bn_server.domain.user.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    // 1. 이름 검색 (User.name)
    public static Specification<User> likeName(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    // 2. 이메일 검색 (User.email)
    public static Specification<User> likeEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("email"), "%" + email + "%");
    }

    // 3. 회사명 검색 (User -> Company 조인)
    public static Specification<User> likeCompanyName(String companyName) {
        return (root, query, criteriaBuilder) -> {
            // User와 Company를 Left Join
            Join<User, Company> companyJoin = root.join("company", JoinType.LEFT);
            return criteriaBuilder.like(companyJoin.get("companyName"), "%" + companyName + "%");
        };
    }

    // 4. 회사 타입 검색 (User -> Company 조인)
    public static Specification<User> equalCompanyType(CompanyType type) {
        return (root, query, criteriaBuilder) -> {
            Join<User, Company> companyJoin = root.join("company", JoinType.LEFT);
            return criteriaBuilder.equal(companyJoin.get("type"), type);
        };
    }
}
package org.etmetmy.bn_server.domain.user.specification;

import jakarta.persistence.criteria.JoinType;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    // 1. 이름 검색
    public static Specification<User> likeName(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    // 2. 이메일 검색
    public static Specification<User> likeEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("email"), "%" + email + "%");
    }

    // 3. 회사 이름 검색
    public static Specification<User> likeCompanyName(String companyName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.join("company", JoinType.LEFT).get("companyName"), "%" + companyName + "%");
    }

    // 4. 회사 타입 검색
    public static Specification<User> equalCompanyType(CompanyType type) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.join("company", JoinType.LEFT).get("type"), type);
    }
}
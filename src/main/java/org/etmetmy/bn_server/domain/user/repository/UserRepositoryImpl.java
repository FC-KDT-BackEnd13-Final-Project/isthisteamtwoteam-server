package org.etmetmy.bn_server.domain.user.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.dto.MemberSearchCondition;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.etmetmy.bn_server.domain.user.entity.QUser.user;
import static org.etmetmy.bn_server.domain.company.entity.QCompany.company;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> search(MemberSearchCondition condition) {
        return queryFactory
                .selectFrom(user)
                .leftJoin(user.company, company).fetchJoin() // 성능 최적화
                .where(
                        usernameEq(condition.getName()),
                        companyNameEq(condition.getCompanyName()),
                        emailEq(condition.getEmail())
                )
                .fetch();
    }

    // 동적 쿼리 조건들 (null이면 무시됨)
    private BooleanExpression usernameEq(String name) {
        return StringUtils.hasText(name) ? user.name.contains(name) : null;
    }

    private BooleanExpression companyNameEq(String companyName) {
        return StringUtils.hasText(companyName) ? company.companyName.contains(companyName) : null;
    }

    private BooleanExpression emailEq(String email) {
        return StringUtils.hasText(email) ? user.email.contains(email) : null;
    }
}
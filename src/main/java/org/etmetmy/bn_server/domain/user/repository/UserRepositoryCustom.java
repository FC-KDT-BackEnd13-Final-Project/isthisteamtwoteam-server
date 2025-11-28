package org.etmetmy.bn_server.domain.user.repository;

import org.etmetmy.bn_server.domain.user.dto.MemberSearchCondition;
import org.etmetmy.bn_server.domain.user.entity.User;
import java.util.List;

public interface UserRepositoryCustom {
    List<User> search(MemberSearchCondition condition);
}
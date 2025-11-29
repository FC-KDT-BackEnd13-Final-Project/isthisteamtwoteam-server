package org.etmetmy.bn_server.domain.user.service;

import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.user.dto.MemberUpdateRequest;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    @Transactional
    void updateMember(Long memberId, MemberUpdateRequest request);

    @Transactional
    void deleteMember(Long memberId);

    List<User> searchMembers(String name, String email, String companyName, String type);
}
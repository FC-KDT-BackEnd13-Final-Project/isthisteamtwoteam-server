package org.etmetmy.bn_server.domain.user.service;

import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.user.dto.MemberUpdateRequest;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    @Transactional
    Long updateMember(Long memberId, MemberUpdateRequest request);

    @Transactional
    Long deleteMember(Long memberId);

    List<User> searchMembers(String name, String email, String companyName, String type);
    Long joinUser(UserDto userDto);
    User login(UserLoginDto loginDto);
}
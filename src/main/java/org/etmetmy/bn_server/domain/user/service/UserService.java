package org.etmetmy.bn_server.domain.user.service;

import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.user.dto.request.UserChangePasswordRequest;
import org.etmetmy.bn_server.domain.user.dto.request.UserUpdateRequest;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.user.dto.response.UserDataResponse;
import org.etmetmy.bn_server.domain.user.dto.response.UserProfileImgNameResponse;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    @Transactional
    Long updateMember(Long memberId, UserUpdateRequest request);

    @Transactional
    Long deleteMember(Long memberId);

    UserDataResponse searchUsers(String name, String email, Pageable pageable);

    Long joinUser(UserDto userDto);
    User login(UserLoginDto loginDto);
    UserProfileImgNameResponse getProfileImgName(Long userId);

    @Transactional
    void changePassword(Long userId, UserChangePasswordRequest request);

}
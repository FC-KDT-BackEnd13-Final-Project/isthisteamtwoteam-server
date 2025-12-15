package org.etmetmy.bn_server.domain.user.service;

import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.user.dto.request.*;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.response.PasswordFindResponse;
import org.etmetmy.bn_server.domain.user.dto.response.PasswordResetResponse;
import org.etmetmy.bn_server.domain.user.dto.response.UserDataResponse;
import org.etmetmy.bn_server.domain.user.dto.response.UserProfileImgNameResponse;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    @Transactional
    Long updateMember(Long memberId, UserUpdateRequest request);

    @Transactional
    Long deleteMember(Long memberId);

    UserDataResponse searchUsers(String name, String email);
    Long joinUser(UserDto userDto);
    User login(UserLoginDto loginDto);
    UserProfileImgNameResponse getProfileImgName(Long userId);

    @Transactional
    void changePassword(Long userId, UserChangePasswordRequest request);

    @Transactional
    PasswordFindResponse sendPasswordResetCode(PasswordFindRequest request);

     //*비밀번호 재설정 - 인증 코드 확인 및 비밀번호 변경
    @Transactional
    PasswordResetResponse resetPassword(PasswordResetVerifyCodeRequest request);

}
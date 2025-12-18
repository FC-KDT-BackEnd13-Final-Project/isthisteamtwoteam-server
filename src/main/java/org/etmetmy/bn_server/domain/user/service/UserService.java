package org.etmetmy.bn_server.domain.user.service;

import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.user.dto.request.*;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.response.*;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional
    PasswordFindResponse sendPasswordResetCode(PasswordFindRequest request);

     //*비밀번호 재설정 - 인증 코드 확인 및 비밀번호 변경
    @Transactional
    PasswordResetResponse resetPassword(PasswordResetVerifyCodeRequest request);

    @Transactional
    UserSelfUpdateResponse updateMyInfo(Long userId, UserSelfUpdateRequest request);

    // 회원정보 수정 - 이미지 업로드
    UserSelfUpdateResponse updateProfileImage(Long userId, MultipartFile image);
}
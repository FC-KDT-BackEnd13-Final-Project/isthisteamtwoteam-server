package org.etmetmy.bn_server.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.user.dto.entity.UserSessionDto;
import org.etmetmy.bn_server.domain.user.dto.request.*;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.response.*;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.service.UserService;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.etmetmy.bn_server.web.SessionConst;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "사용자 인증 및 관리 API")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    //todo : 회원 계정 생성
    @Operation(summary = "회원 계정 생성", description = "새로운 회원 계정을 생성합니다 (관리자용)")
    @PostMapping("/admin/user")
    public CommonResponse<Long> joinUser(
            @RequestBody UserDto userDto
    ){
        Long userId = userService.joinUser(userDto);

        return CommonResponse.success("성공적으로 회원이 생성되었습니다.",userId);
    }

    //todo: 로그인
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다")
    @PostMapping("/login")
    public CommonResponse<Long> login(
            @Valid @RequestBody UserLoginDto userLoginDto,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) User loginUser,
            HttpServletRequest request
    ) {
        // 이미 로그인된 경우 체크
        if (loginUser != null) {
            return CommonResponse.success("이미 로그인되어 있습니다.", loginUser.getId());
        }

        // 로그인 처리
        User user = userService.login(userLoginDto);

        // 새 세션 생성 및 사용자 정보 저장
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(3600);
        session.setAttribute(SessionConst.LOGIN_MEMBER, user);
        session.setAttribute("userId", user.getId());
        session.setAttribute(SessionConst.USER_ROLE, user.getRole());  // Role 저장 추가

        return CommonResponse.success("성공적으로 로그인을 완료했습니다.", user.getId());
    }

    //todo: 로그아웃
    @Operation(summary = "로그아웃", description = "현재 세션을 종료하고 로그아웃합니다")
    @PostMapping("/logout")
    public CommonResponse<Object> logout(
            HttpServletRequest request
    ){
        // 세션 없다면 새로운 세션 생성하지 않음
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
        return CommonResponse.success("성공적으로 로그아웃을 완료했습니다.");

    }

    //todo: 회원 조회
    @Operation(summary = "회원 조회", description = "이름 또는 이메일로 회원을 검색합니다 (관리자용)")
    @GetMapping("/admin/users")
    public CommonResponse<UserDataResponse> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        UserDataResponse usersBoardData = userService.searchUsers(name, email, pageable);
        return CommonResponse.success("대시보드 데이터를 성공적으로 조회하였습니다.", usersBoardData);
    }

    //todo: 회원 삭제 (DELETE)
    @Operation(summary = "회원 삭제", description = "회원을 삭제합니다 (관리자용)")
    @DeleteMapping("/admin/users/{memberId}")
    public CommonResponse<Long> deleteMember(@PathVariable Long memberId) {
        Long userId = userService.deleteMember(memberId);
        return CommonResponse.success("회원 삭제 완료",userId);
    }

    //todo: 회원 정보 수정 (PUT)
    @Operation(summary = "회원 정보 수정", description = "회원 정보를 수정합니다 (관리자용)")
    @PutMapping("/admin/users/{memberId}")
    public CommonResponse<Long> updateMember(
            @PathVariable Long memberId,
            @RequestBody UserUpdateRequest request
    ) {
        Long userId = userService.updateMember(memberId, request);

        return CommonResponse.success("회원 정보 수정 완료",userId);
    }

    //todo: 자신의 비밀번호 변경 (PUT)
    @Operation(summary = "비밀번호 변경", description = "자신의 비밀번호를 변경합니다")
    @PutMapping("/users/password")
    public CommonResponse<Long> changeMyPassword(
            @RequestBody UserChangePasswordRequest request, HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        // 비밀번호 변경 처리
        userService.changePassword(loginUserId, request);

        return CommonResponse.success("비밀번호가 성공적으로 변경되었습니다.", null);
    }


    // todo: 비밀번호 찾기 - 이메일 인증코드 전송
    @Operation(summary = "비밀번호 찾기 - 인증 코드 발송", description = "이메일로 비밀번호 재설정 인증 코드를 발송합니다")
    @PostMapping("/password/find")
    public CommonResponse<PasswordFindResponse> sendPasswordResetCode(
            @Valid @RequestBody PasswordFindRequest request
    ) {
        PasswordFindResponse response = userService.sendPasswordResetCode(request);
        return CommonResponse.success("인증 코드가 이메일로 발송되었습니다.", response);
    }

    // todo: 비밀번호 찾기 - 인증코드 확인 및 비밀번호 재설정
    @Operation(summary = "비밀번호 재설정", description = "인증 코드 확인 후 새 비밀번호로 재설정합니다")
    @PostMapping("/password/reset")
    public CommonResponse<PasswordResetResponse> resetPassword(
            @Valid @RequestBody PasswordResetVerifyCodeRequest request
    ) {
        PasswordResetResponse response = userService.resetPassword(request);
        return CommonResponse.success("비밀번호가 성공적으로 변경되었습니다.", response);
    }

    // todo: 회원정보 수정
    @Operation(summary = "본인 정보 수정", description = "로그인한 사용자가 자신의 정보를 수정합니다")
    @PutMapping("/users")
    public CommonResponse<UserSelfUpdateResponse> updateMyInfo(@RequestBody UserSelfUpdateRequest request, HttpSession session) {
        Long loginUserId = SessionUtil.getLoginUserId(session);

        UserSelfUpdateResponse response = userService.updateMyInfo(loginUserId, request);

        return CommonResponse.success("사용자 정보 수정 완료", response);
    }

    // todo: 회원정보 수정 - 이미지 업로드
    @Operation(summary = "본인 이미지 수정", description = "로그인한 사용자가 자신의 프로필이미지를 수정합니다")
    @PutMapping("/users/profile-image")
    public CommonResponse<UserSelfUpdateResponse> uploadProfileImage(
            @RequestPart MultipartFile image,
            HttpSession session
    ) {
        Long userId = SessionUtil.getLoginUserId(session);
        UserSelfUpdateResponse response = userService.updateProfileImage(userId, image);
        return CommonResponse.success("프로필 이미지 변경 완료", response);
    }

    /**
     * 회원의 프로필 사진과 회원명 조회
     * */
    @Operation(summary = "프로필 정보 조회", description = "사이드바에 표시될 프로필 사진과 회원명을 조회합니다")
    @GetMapping("/users/profile/sidebar")
    public CommonResponse<Object> getProfileImageAndUsername(
            HttpServletRequest request
    ){
        HttpSession session = request.getSession(false);
        Long loginUserId = SessionUtil.getLoginUserId(session);

        if(loginUserId == null){
            return CommonResponse.fail("로그인이 필요합니다.");
        }else{
            UserProfileImgNameResponse profile = userService.getProfileImgName(loginUserId);
            return CommonResponse.success("성공적으로 조회했습니다",profile);
        }
    }

    /**
     * 프론트엔드에서 세션 확인할 때 사용되는 api
     * */
    @Operation(summary = "세션 확인", description = "현재 로그인된 사용자의 세션 정보를 조회합니다")
    @GetMapping("/auth/session")
    public CommonResponse<UserSessionDto> getSession(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) User loginUser
    ) {
        if (loginUser == null) {
            throw new UserNotFoundException("로그인이 필요합니다.");
        }

        return CommonResponse.success("세션 조회 성공",
                new UserSessionDto(loginUser.getId(), loginUser.getName(), loginUser.getRole())
        );
    }
}
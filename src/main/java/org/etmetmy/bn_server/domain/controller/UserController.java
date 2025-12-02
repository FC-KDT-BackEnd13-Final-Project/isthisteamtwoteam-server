package org.etmetmy.bn_server.domain.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dto.MemberUpdateRequest;
import org.etmetmy.bn_server.domain.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.entity.User;
import org.etmetmy.bn_server.domain.service.UserService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.web.SessionConst;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    //todo : 회원 계정 생성
    @PostMapping("/admin/user")
    public CommonResponse<Long> joinUser(
            @RequestBody UserDto userDto
    ){
        Long userId = userService.joinUser(userDto);

        return CommonResponse.success("성공적으로 회원이 생성되었습니다.",userId);
    }

    //todo: 로그인
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
        session.setAttribute(SessionConst.USER_ROLE, user.getRole());  // Role 저장 추가

        return CommonResponse.success("성공적으로 로그인을 완료했습니다.", user.getId());
    }

    //todo: 로그아웃
    @GetMapping("/logout")
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

    // 회원 조회 & 검색 API
    @GetMapping
    public CommonResponse<List<User>> getMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String type
    ) {

        List<User> members = userService.searchMembers(name, email, companyName, type);
        return CommonResponse.success("회원을 성공적으로 조회하였습니다.", members);
    }


    //회원 삭제 (DELETE)
    @DeleteMapping("/{memberId}")
    public CommonResponse<Long> deleteMember(@PathVariable Long memberId) {
        Long userId = userService.deleteMember(memberId);
        return CommonResponse.success("회원 삭제 완료",userId);
    }


    //회원 정보 수정 (PUT)
    @PutMapping("/{memberId}")
    public CommonResponse<Long> updateMember(
            @PathVariable Long memberId,
            @RequestBody MemberUpdateRequest request
    ) {
        Long userId = userService.updateMember(memberId, request);

        return CommonResponse.success("회원 정보 수정 완료",userId);
    }



}
package org.etmetmy.bn_server.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.service.UserService;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.web.SessionConst;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1")
@RequiredArgsConstructor
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
    @GetMapping("/users/login")
    public String login(
            @RequestBody UserLoginDto userLoginDto,
            HttpServletRequest request
    ){

        User user = userService.login(userLoginDto);

        // 로그인 실패 시 에러 반환
        if(user == null){
            // 에러 처리 해주기
            return null;
        }


        // 로그인 성공 시 세션이 존재한다면 기존 세션 반환함
        // 세션 없다면 새로운 세션 생성해서 반환함
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, user);

    }


}

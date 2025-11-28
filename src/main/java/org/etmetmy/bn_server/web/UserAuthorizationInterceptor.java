package org.etmetmy.bn_server.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class UserAuthorizationInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        if(session ==null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null){
            log.info("로그인 인증 실패");

            response.sendRedirect("http://localhost:5173/home");
            return false;
        }
        return true;
    }
}

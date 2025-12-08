package org.etmetmy.bn_server.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.global.CommonResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
public class RoleAuthorizationInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        log.info("로그인 여부 인증 체크 인터셉터 실행: {}", requestURI);  // 수정

        HttpSession session = request.getSession(false);

        // 로그인 체크
        if(session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null){
            log.info("로그인 인증 실패");
            sendUnauthorizedResponse(response, "로그인이 필요합니다.");  // 수정
            return false;
        }

        // 권한 체크
        Role userRole = (Role) session.getAttribute(SessionConst.USER_ROLE);
        Role requiredRole = getRequiredRole(requestURI);

        if(requiredRole != null && !hasPermission(userRole, requiredRole)){  // 순서 수정
            log.info("권한이 없습니다. 필요한 권한: {}, 유저의 권한: {}", requiredRole, userRole);
            sendForbiddenResponse(response, "접근 권한이 없습니w다.");  // 추가
            return false;
        }

        log.info("권한 체크 통과: URI={}, 사용자 권한={}", requestURI, userRole);
        return true;
    }


    //todo: url 에 따른 필요 권한 반환
    private Role getRequiredRole(String requestURI) {
        if(requestURI.startsWith("/api/v1/admin")){  // admin을 먼저 체크해야 함
            return Role.ADMIN;
        } else if(requestURI.startsWith("/api/v1/developers")){
            return Role.DEVELOPER;
        } else if(requestURI.startsWith("/api/v1/users")){
            return Role.CUSTOMER;
        }
        return null;
    }

    //todo: url 을 요청하는 사용자에게 권한이 있는지
    private boolean hasPermission(Role userRole, Role requiredRole) {  // 순서 수정
        if (userRole == null) {
            return false;
        }
        return userRole.getLevel() >= requiredRole.getLevel();
    }



    // 401 Unauthorized - 로그인 필요
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        CommonResponse<Object> commonResponse = CommonResponse.fail(message);
        String json = objectMapper.writeValueAsString(commonResponse);
        response.getWriter().write(json);
    }

    // 403 Forbidden - 권한 부족
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 수정: UNAUTHORIZED -> FORBIDDEN
        response.setContentType("application/json;charset=UTF-8");

        CommonResponse<Object> commonResponse = CommonResponse.fail(message);
        String json = objectMapper.writeValueAsString(commonResponse);
        response.getWriter().write(json);
    }
}
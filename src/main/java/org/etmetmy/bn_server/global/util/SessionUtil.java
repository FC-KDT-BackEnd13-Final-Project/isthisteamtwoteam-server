package org.etmetmy.bn_server.global.util;

import jakarta.servlet.http.HttpSession;
import org.etmetmy.bn_server.domain.entity.User;
import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;
import org.etmetmy.bn_server.web.SessionConst;

/**
 * 세션 관련 유틸리티 클래스
 * 로그인한 사용자 정보를 세션에서 안전하게 추출합니다.
 */
public class SessionUtil {

    /**
     * 로그인 사용자 조회 (없으면 null 반환)
     */
    public static User getLoginUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(SessionConst.LOGIN_MEMBER);
    }

    /**
     * 로그인 사용자 조회 (없으면 예외 발생)
     */
    public static User getLoginUserOrThrow(HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            throw new CustomException(StatusCode.USER_NOT_LOGIN);
        }
        return loginUser;
    }

    /**
     * 로그인 사용자 ID 조회
     */
    public static Long getLoginUserId(HttpSession session) {
        return getLoginUserOrThrow(session).getId();
    }

    /**
     * 로그인 여부 확인
     */
    public static boolean isLoggedIn(HttpSession session) {
        return getLoginUser(session) != null;
    }

}
package org.etmetmy.bn_server.global.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // 프록시를 통과하는 경우 쉼표로 구분된 IP 목록이 올 수 있으므로, 첫 번째 IP를 사용
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
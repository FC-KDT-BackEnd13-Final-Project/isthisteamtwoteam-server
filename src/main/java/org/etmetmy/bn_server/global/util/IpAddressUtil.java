package org.etmetmy.bn_server.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class IpAddressUtil {

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

    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (StringUtils.hasLength(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 여러 IP를 포함할 수 있으므로, 첫 번째 IP를 사용
                int commaIndex = ip.indexOf(',');
                if (commaIndex > 0) {
                    return ip.substring(0, commaIndex);
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}
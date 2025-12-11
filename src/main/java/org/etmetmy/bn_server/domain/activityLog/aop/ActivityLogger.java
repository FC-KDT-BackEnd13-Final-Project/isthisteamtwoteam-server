package org.etmetmy.bn_server.domain.activityLog.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD) // 메서드에만 적용
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지
@Documented
public @interface ActivityLogger {

    // 로깅 대상 엔티티의 타입 ("Post", "Project")
    String targetType();

    // 수행된 활동의 종류 (ActivityAction Enum)
    String action(); // Enum이 아닌 String으로 받아서 AOP에서 변환
}
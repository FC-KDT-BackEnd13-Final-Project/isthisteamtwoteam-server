package org.etmetmy.bn_server.global.aop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.etmetmy.bn_server.domain.activityLog.aop.ActivityLogger;
import org.etmetmy.bn_server.domain.activityLog.event.ActivityLogEvent;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.dto.response.ReplyPostCreateResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectCreateResponse;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.IpAddressUtil;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class ActivityLogAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Pointcut("@annotation(org.etmetmy.bn_server.domain.activityLog.aop.ActivityLogger)")
    public void activityLoggerPointcut() {}

    @AfterReturning(pointcut = "activityLoggerPointcut() && @annotation(activityLogger)", returning = "result")
    public void saveActivityLog(JoinPoint joinPoint, ActivityLogger activityLogger, Object result) {

        // 1. User ID, IP, Project ID, Target ID
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();

        String ipAddress = IpAddressUtil.getClientIp(request);
        Long userId = extractUserIdFromSession(request);

        if (userId == null) {
            log.warn("로그 저장 실패: 세션에서 사용자 ID를 찾을 수 없음.");
            return;
        }

        Long projectId = findProjectId(joinPoint, result);
        String targetType = activityLogger.targetType();
        Long targetId = findTargetId(joinPoint, result, targetType);

        if (projectId == null || targetId == null) {
            log.warn("로그 저장 실패: Project ID ({}) 또는 Target ID ({})를 찾을 수 없음. TargetType={}",
                    projectId, targetId, targetType);
            return;
        }

        // 2. 최소 정보만 담아 이벤트 발행
        eventPublisher.publishEvent(
                new ActivityLogEvent(
                        projectId,
                        userId,
                        ActivityAction.valueOf(activityLogger.action()),
                        targetType,
                        targetId,
                        ipAddress
                )
        );

        log.info("ActivityLogEvent 발행 완료: Action={}, TargetType={}, TargetId={}",
                activityLogger.action(), targetType, targetId);
    }

    /**
     * CommonResponse로 래핑된 결과를 언래핑
     */
    private Object unwrapCommonResponse(Object result) {
        if (result instanceof CommonResponse<?> commonResponse) {
            Object unwrapped = commonResponse.getResponse();
            log.debug("Unwrapped CommonResponse. Original type: {}, Unwrapped type: {}",
                    result.getClass().getName(),
                    unwrapped != null ? unwrapped.getClass().getName() : "null");
            return unwrapped;
        }
        return result;
    }

    private Long extractUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        User loginUser = SessionUtil.getLoginUser(session);
        return (loginUser != null) ? loginUser.getId() : null;
    }

    private String findTargetType(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ActivityLogger annotation = method.getAnnotation(ActivityLogger.class);
        return annotation != null ? annotation.targetType() : null;
    }

    private Long findProjectId(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // 1. 파라미터에서 projectId 찾기
        for (int i = 0; i < parameterNames.length; i++) {
            if ("projectId".equals(parameterNames[i]) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }

        // 2. CommonResponse 언래핑
        Object unwrapped = unwrapCommonResponse(result);

        // 3. ProjectCreateResponse에서 projectId 추출
        if (unwrapped instanceof ProjectCreateResponse projectResponse) {
            return projectResponse.getProjectId();
        }

        // 4. 반환값이 Long이고 targetType이 Project인 경우
        if (unwrapped instanceof Long && "Project".equals(findTargetType(joinPoint))) {
            return (Long) unwrapped;
        }

        return null;
    }

    private Long findTargetId(JoinPoint joinPoint, Object result, String targetType) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String targetIdParamName = null;
        if ("User".equalsIgnoreCase(targetType)) {
            targetIdParamName = "userId";
        } else if ("Post".equalsIgnoreCase(targetType)) {
            targetIdParamName = "postId";
        } else if ("Project".equalsIgnoreCase(targetType)) {
            return findProjectId(joinPoint, result);
        }

        // 1. 파라미터에서 targetId 찾기
        if (targetIdParamName != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (targetIdParamName.equals(parameterNames[i]) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        }

        // 2. 반환 값에서 targetId 찾기 (CommonResponse 언래핑)
        Object unwrapped = unwrapCommonResponse(result);

        if ("Post".equalsIgnoreCase(targetType)) {
            if (unwrapped instanceof PostCreateResponse postResponse) {
                return postResponse.getPostId();
            }
        }

        if ("Project".equalsIgnoreCase(targetType)) {
            if (unwrapped instanceof ProjectCreateResponse projectResponse) {
                return projectResponse.getProjectId();
            }
        }

        // 3. 일반적인 Long 타입 반환 값 처리
        if (unwrapped instanceof Long) {
            return (Long) unwrapped;
        }

        log.warn("Could not find targetId. TargetType={}, Result type={}",
                targetType,
                unwrapped != null ? unwrapped.getClass().getName() : "null");
        return null;
    }
}
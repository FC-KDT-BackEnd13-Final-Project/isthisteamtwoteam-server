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

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();

        String ipAddress = IpAddressUtil.getClientIp(request);
        Long userId = extractUserIdFromSession(request);

        if (userId == null) {
            log.warn("로그 저장 실패: 세션에서 사용자 ID를 찾을 수 없음.");
            return;
        }

        // CommonResponse 언래핑
        Object actualResult = unwrapCommonResponse(result);

        Long projectId = findProjectId(joinPoint, actualResult);
        String targetType = activityLogger.targetType();
        Long targetId = findTargetId(joinPoint, actualResult, targetType);

        if (projectId == null || targetId == null) {
            log.warn("로그 저장 실패: Project ID ({}) 또는 Target ID ({})를 찾을 수 없음. TargetType={}",
                    projectId, targetId, targetType);
            return;
        }

        // 추가 정보 조회
        String userName = getUserName(userId);
        String projectName = getProjectName(projectId);

        // 한글 설명 생성
        String description = generateDescription(
                userName,
                projectName,
                ActivityAction.valueOf(activityLogger.action()),
                targetType,
                targetId
        );

        // 이벤트 발행
        eventPublisher.publishEvent(
                new ActivityLogEvent(
                        projectId,
                        projectName,
                        userId,
                        userName,
                        ActivityAction.valueOf(activityLogger.action()),
                        targetType,
                        targetId,
                        ipAddress,
                        description
                )
        );

        log.info("Activity: {}", description);
    }

    /**
     * CommonResponse로 래핑된 결과를 언래핑
     */
    private Object unwrapCommonResponse(Object result) {
        if (result instanceof CommonResponse<?> commonResponse) {
            Object unwrapped = commonResponse.getResponse();  // ← getData()가 아닌 getResponse()
            log.debug("Unwrapped CommonResponse. Original type: {}, Unwrapped type: {}",
                    result.getClass().getName(),
                    unwrapped != null ? unwrapped.getClass().getName() : "null");
            return unwrapped;
        }
        return result;
    }

    /**
     * 한글 설명 생성
     */
    private String generateDescription(
            String userName,
            String projectName,
            ActivityAction action,
            String targetType,
            Long targetId
    ) {
        String actionKorean = switch (action) {
            case CREATE -> "생성";
            case UPDATE -> "수정";
            case DELETE -> "삭제";
            case APPROVE -> "승인";
            case REJECT -> "거절";
            case COMPLETE -> "완료 처리";
            default -> action.name();
        };

        String targetTypeKorean = switch (targetType.toLowerCase()) {
            case "post" -> "게시글";
            case "comment" -> "댓글";
            case "project" -> "프로젝트";
            case "user" -> "사용자";
            default -> targetType;
        };

        return String.format("%s님이 프로젝트 \"%s\"의 %s #%d을(를) %s했습니다.",
                userName, projectName, targetTypeKorean, targetId, actionKorean);
    }

    /**
     * 사용자 이름 조회
     */
    private String getUserName(Long userId) {
        try {
            return userRepository.findById(userId)
                    .map(User::getName)
                    .orElse("알 수 없는 사용자");
        } catch (Exception e) {
            log.warn("사용자 이름 조회 실패 (userId: {}): {}", userId, e.getMessage());
            return "사용자#" + userId;
        }
    }

    /**
     * 프로젝트 이름 조회
     */
    private String getProjectName(Long projectId) {
        try {
            return projectRepository.findById(projectId)
                    .map(Project::getProjectName)
                    .orElse("알 수 없는 프로젝트");
        } catch (Exception e) {
            log.warn("프로젝트 이름 조회 실패 (projectId: {}): {}", projectId, e.getMessage());
            return "프로젝트#" + projectId;
        }
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

        for (int i = 0; i < parameterNames.length; i++) {
            if ("projectId".equals(parameterNames[i]) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }

        if (result instanceof Long && "Project".equals(findTargetType(joinPoint))) {
            return (Long) result;
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

        // 2. 반환 값에서 targetId 찾기
        if ("Post".equalsIgnoreCase(targetType)) {
            if (result instanceof PostCreateResponse postResponse) {
                return postResponse.getPostId();
            }
            if (result instanceof ReplyPostCreateResponse replyResponse) {
                return replyResponse.getPostId();
            }
        }

        // 3. 일반적인 Long 타입 반환 값 처리
        if (result instanceof Long) {
            return (Long) result;
        }

        log.warn("Could not find targetId. TargetType={}, Result type={}",
                targetType,
                result != null ? result.getClass().getName() : "null");
        return null;
    }
}
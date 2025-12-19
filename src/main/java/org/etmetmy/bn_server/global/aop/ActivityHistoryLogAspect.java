package org.etmetmy.bn_server.global.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.etmetmy.bn_server.domain.activityLog.aop.ActivityHistoryLogger;
import org.etmetmy.bn_server.domain.activityLog.event.ActivityLogEvent;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.dto.response.ReplyPostCreateResponse;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectCreateResponse;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.IpAddressUtil;
import org.etmetmy.bn_server.web.SessionConst;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class ActivityHistoryLogAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ProjectRepository projectRepository;
    private final PostRepository postRepository;

    @Pointcut("@annotation(org.etmetmy.bn_server.domain.activityLog.aop.ActivityHistoryLogger)")
    public void activityHistoryLoggerPointcut() {}

    @Around("activityHistoryLoggerPointcut() && @annotation(activityHistoryLogger)")
    public Object saveActivityLog(ProceedingJoinPoint joinPoint, ActivityHistoryLogger activityHistoryLogger) throws Throwable {
        // 1. 실제 메서드 실행
        Object result = joinPoint.proceed();

        // 2. Activity Log 저장 처리
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ipAddress = IpAddressUtil.getClientIp(request);
            Long userId = extractUserIdFromSession(request);

            if (userId != null) {
                String action = activityHistoryLogger.action();
                String targetType = activityHistoryLogger.targetType();
                Long targetId = findTargetId(result, targetType);
                Long projectId = findProjectId(joinPoint, result, targetType);

                eventPublisher.publishEvent(new ActivityLogEvent(
                        projectId, userId, ActivityAction.valueOf(action), targetType, targetId, ipAddress, null
                ));
            }
        } catch (Exception e) {
            log.error("ActivityLog 저장 중 오류 발생", e);
        }

        return result;
    }

    private Long findTargetId(Object result, String targetType) {
        Object unwrapped = unwrapCommonResponse(result);

        // ProjectCreateResponse인 경우
        if (unwrapped instanceof ProjectCreateResponse projectResponse) {
            return projectResponse.getProjectId();
        }

        // PostCreateResponse인 경우
        if (unwrapped instanceof PostCreateResponse postResponse) {
            return postResponse.getPostId();
        }

        // ReplyPostCreateResponse인 경우
        if (unwrapped instanceof ReplyPostCreateResponse replyPostResponse) {
            return replyPostResponse.getPostId();
        }

        // Long 타입인 경우
        if (unwrapped instanceof Long) {
            return (Long) unwrapped;
        }

        return null;
    }

    private Long findProjectId(ProceedingJoinPoint joinPoint, Object result, String targetType) {
        // 1. 메서드 파라미터에서 projectId 찾기
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long projectId) {
                // 간단히 첫 번째 Long을 projectId로 가정
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null) {
                    return projectId;
                }
            }
        }

        // 2. 반환값에서 projectId 추출
        Object unwrapped = unwrapCommonResponse(result);

        // 3. ProjectCreateResponse인 경우
        if (unwrapped instanceof ProjectCreateResponse projectResponse) {
            return projectResponse.getProjectId();
        }

        // 4. PostCreateResponse인 경우 postId로 Post 조회하여 projectId 가져오기
        if (unwrapped instanceof PostCreateResponse postResponse) {
            return postRepository.findById(postResponse.getPostId())
                    .map(post -> post.getProject() != null ? post.getProject().getId() : null)
                    .orElse(null);
        }

        // 5. 반환값이 Long이고 targetType이 Project인 경우
        if (unwrapped instanceof Long && "Project".equals(targetType)) {
            return (Long) unwrapped;
        }

        return null;
    }

    private Object unwrapCommonResponse(Object result) {
        if (result instanceof CommonResponse<?> commonResponse) {
            return commonResponse.getResponse();
        }
        return result;
    }

    private Long extractUserIdFromSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) return null;
        Object userObj = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (userObj instanceof User user) {
            return user.getId();
        }
        return null;
    }
}

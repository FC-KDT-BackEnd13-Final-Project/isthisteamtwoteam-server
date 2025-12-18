package org.etmetmy.bn_server.global.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.etmetmy.bn_server.domain.activityLog.aop.ActivityLogger;
import org.etmetmy.bn_server.domain.activityLog.event.ActivityLogEvent;
import org.etmetmy.bn_server.domain.activityLog.enums.ActivityAction;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.comment.repository.CommentRepository;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.post.repository.StageRepository;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectStageUpdateRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectCreateResponse;
import org.etmetmy.bn_server.domain.project.repository.ProjectStageRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.global.CommonResponse;
import org.etmetmy.bn_server.global.util.IpAddressUtil;
import org.etmetmy.bn_server.web.SessionConst;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class ActivityLogAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ProjectRepository projectRepository;
    private final StageRepository stageRepository;
    private final ProjectStageRepository projectStageRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> TARGET_TYPE_PARAM_MAP = Map.of(
            "User", "userId",
            "Post", "postId",
            "Project", "projectId",
            "ProjectMember", "userId",
            "Comment", "commentId"
    );

    @Pointcut("@annotation(org.etmetmy.bn_server.domain.activityLog.aop.ActivityLogger)")
    public void activityLoggerPointcut() {}

    @Around("activityLoggerPointcut() && @annotation(activityLogger)")
    public Object saveActivityLog(ProceedingJoinPoint joinPoint, ActivityLogger activityLogger) throws Throwable {
        String action = activityLogger.action();
        String targetType = activityLogger.targetType();
        String oldContent = "";

        // 1. 메서드 실행 전: 수정(UPDATE)이라면 DB에서 원본 데이터를 미리 선점
        if ("UPDATE".equals(action)) {
            try {
                Long targetId = findTargetIdBeforeExecution(joinPoint, targetType);
                if (targetId != null) {
                    oldContent = fetchCurrentNameOrContent(targetType, targetId);
                    log.debug("수정 전 원본 데이터 확보: {}", oldContent);
                }
            } catch (Exception e) {
                log.warn("Before Advice 원본 데이터 추출 실패", e);
            }
        }

        // 2. 실제 비즈니스 로직(Service) 실행
        Object result = joinPoint.proceed();

        // 3. 메서드 실행 후: 나머지 정보 추출 및 이벤트 발행
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ipAddress = IpAddressUtil.getClientIp(request);
            Long userId = extractUserIdFromSession(request);

            if (userId != null) {
                Long targetId = findTargetId(joinPoint, result, targetType);
                Long projectId = findProjectId(joinPoint, result, targetType, targetId);

                // ProjectMember는 targetId가 userId (추가된 사용자 ID)
                String detail = extractDetailWithOldContent(joinPoint, targetType, targetId, action, oldContent);

                eventPublisher.publishEvent(new ActivityLogEvent(
                        projectId, userId, ActivityAction.valueOf(action), targetType, targetId, ipAddress, detail
                ));
            }
        } catch (Exception e) {
            log.error("ActivityLog 사후 처리 중 오류", e);
        }

        return result;
    }

    // 실행 전 파라미터에서만 ID 추출
    private Long findTargetIdBeforeExecution(JoinPoint joinPoint, String targetType) {
        String paramName = TARGET_TYPE_PARAM_MAP.get(targetType);
        return (paramName != null) ? extractIdFromArgs(joinPoint, paramName) : null;
    }

    // 미리 받아온 oldContent를 사용
    private String extractDetailWithOldContent(JoinPoint joinPoint, String targetType, Long targetId, String action, String oldContent) {
        Map<String, Object> data = new HashMap<>();

        if ("DELETE".equals(action)) {
            data.put("content", oldContent.isEmpty() ? fetchCurrentNameOrContent(targetType, targetId) : oldContent);
            return toJson(data);
        }

        Object requestDto = findDtoFromArgs(joinPoint.getArgs());
        if (requestDto == null) return null;

        try {
            JsonNode node = objectMapper.valueToTree(requestDto);

            // 단계 변경(Stage) 체크
            if ("Project".equals(targetType) && node.has("stageId") && !node.get("stageId").isNull()) {
                Long newStageId = node.get("stageId").asLong();

                // 기존 단계 이름
                String oldStageName = projectRepository.findCurrentStageNameByProjectId(targetId);
                // 새 단계 이름
                String newStageName = stageRepository.findById(newStageId)
                        .map(Stage::getStageName).orElse("알 수 없음");

                data.put("type", "STAGE");
                data.put("old", oldStageName != null ? oldStageName : "미정");
                data.put("new", newStageName);

                log.info("단계 변경 감지: {} -> {}", oldStageName, newStageName);
                return toJson(data);
            }

            // 프로젝트명/게시글 제목 등의 UPDATE
            String currentContent = extractTitleFromDto(requestDto);

            if ("UPDATE".equals(action)) {
                // 빈 값이면 단계 변경 DTO로 간주하고 무시
                if (currentContent == null || currentContent.trim().isEmpty()) {
                    return null;
                }

                data.put("old", oldContent);
                data.put("new", currentContent);
            } else {
                data.put("content", currentContent);
            }

            return toJson(data);
        } catch (Exception e) {
            log.error("Detail 추출 실패", e);
            return null;
        }
    }

    private String fetchCurrentNameOrContent(String targetType, Long targetId) {
        if (targetId == null) return "";
        try {
            return switch (targetType) {
                case "Project" -> projectRepository.findProjectNameById(targetId);
                case "Post" -> postRepository.findTitleById(targetId);
                case "Comment" -> commentRepository.findContentById(targetId);
                default -> "";
            };
        } catch (Exception e) {
            log.warn("원본 데이터 DB 직접 조회 실패: {}", e.getMessage());
            return "";
        }
    }

    private Long findProjectId(JoinPoint joinPoint, Object result, String targetType, Long targetId) {
        // 1. 파라미터 우선 확인
        Long pid = extractIdFromArgs(joinPoint, "projectId");
        if (pid != null) return pid;

        // 2. 프로젝트 타입인 경우 타겟 아이디가 곧 프로젝트 아이디
        if ("Project".equalsIgnoreCase(targetType)) return targetId;

        // 3. 역추적 (Post/Comment -> Project)
        if (targetId != null) {
            if ("Post".equals(targetType)) {
                return postRepository.findById(targetId).map(p -> p.getProject().getId()).orElse(null);
            }
            if ("Comment".equals(targetType)) {
                return commentRepository.findProjectIdByCommentId(targetId);
            }
        }
        return null;
    }

    private Long findTargetId(JoinPoint joinPoint, Object result, String targetType) {
        // 파라미터에서 먼저 찾기 (수정/삭제 시 가장 확실함)
        String paramName = TARGET_TYPE_PARAM_MAP.get(targetType);
        if (paramName != null) {
            Long idFromParam = extractIdFromArgs(joinPoint, paramName);
            if (idFromParam != null) {
                log.debug("TargetId from param: {} = {}", paramName, idFromParam);
                return idFromParam;
            }
        }

        // 2. Response에서 찾기
        Object unwrapped = unwrapCommonResponse(result);

        if (unwrapped instanceof ProjectCreateResponse res) return res.getProjectId();
        if (unwrapped instanceof PostCreateResponse res) return res.getPostId();
        if (unwrapped instanceof CommentResponse res) return res.getCommentId();

        // 3. ProjectMember는 Integer 반환하므로 여기서 처리 불가
        // -> 파라미터의 userId를 targetId로 사용
        log.warn("TargetId를 찾을 수 없음: targetType={}, resultType={}",
                targetType, unwrapped != null ? unwrapped.getClass().getSimpleName() : "null");

        return null;
    }

    private String extractTitleFromDto(Object dto) {
        try {
            JsonNode node = objectMapper.valueToTree(dto);
            if (node.has("projectName")) return node.get("projectName").asText();
            if (node.has("title")) return node.get("title").asText();
            if (node.has("content")) return node.get("content").asText();
            if (node.has("name")) return node.get("name").asText();
        } catch (Exception e) { return ""; }
        return "";
    }

    private Long extractIdFromArgs(JoinPoint joinPoint, String paramName) {
        String[] names = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < names.length; i++) {
            if (paramName.equals(names[i]) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        return null;
    }

    private Object findDtoFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg != null && isDto(arg)) return arg;
        }
        return null;
    }

    private boolean isDto(Object arg) {
        if (arg == null) return false;
        String name = arg.getClass().getSimpleName();
        return name.contains("Request") ||
                name.contains("Dto") ||
                name.contains("Update") ||
                name.contains("ProjectStage") ||
                name.contains("DTO");
    }

    private Object unwrapCommonResponse(Object result) {
        if (result instanceof CommonResponse<?> res) return res.getResponse();
        return result;
    }

    private Long extractUserIdFromSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) return null;
        Object userObj = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (userObj instanceof User user) return user.getId();
        return (Long) session.getAttribute("userId");
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); } catch (Exception e) { return null; }
    }
}
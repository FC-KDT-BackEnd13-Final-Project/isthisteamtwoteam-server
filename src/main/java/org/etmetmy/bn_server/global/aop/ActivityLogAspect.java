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
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.comment.repository.CommentRepository;
import org.etmetmy.bn_server.domain.post.dto.response.PostCreateResponse;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.post.repository.StageRepository;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectAddCheckListRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateCheckListRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectStageUpdateRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectCreateResponse;
import org.etmetmy.bn_server.domain.project.repository.ProjectCheckListRepository;
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
import java.util.List;
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
    private final CheckListRepository checkListRepository;
    private final ProjectCheckListRepository projectCheckListRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> TARGET_TYPE_PARAM_MAP = Map.of(
            "User", "userId",
            "Post", "postId",
            "Project", "projectId",
            "ProjectMember", "userId",
            "Comment", "commentId",
            "CheckList", "checkListId"
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
        String methodName = joinPoint.getSignature().getName();
        String lowerMethodName = methodName.toLowerCase();

        // 1. 순수 삭제(DELETE) 처리 (action이 DELETE인 경우)
        if ("DELETE".equals(action)) {
            data.put("content", oldContent.isEmpty() ? fetchCurrentNameOrContent(targetType, targetId) : oldContent);
            return toJson(data);
        }

        // 2. 체크리스트 할당 및 삭제 (프로젝트)
        if (lowerMethodName.contains("checklist")) {
            // 1. 삭제 로직 (deleteCheckLists 대응)
            if (lowerMethodName.contains("delete") || lowerMethodName.contains("remove")) {
                data.put("action", "checklist_removed");
                Long clId = extractIdFromArgs(joinPoint, "checkListId");
                if (clId != null) {
                    String actualContent = checkListRepository.findById(clId)
                            .map(cl -> cl.getContent()).orElse("알 수 없는 항목");
                    data.put("content", actualContent);
                }
                return toJson(data); // 처리가 끝났으므로 즉시 반환
            }

            // 2. 추가 및 생성 로직 (addCheckLists & createCheckLists 대응)
            data.put("action", "checklist_added");
            Object requestDto = findDtoFromArgs(joinPoint.getArgs());

            // Case A: 기존 체크리스트 ID 리스트로 추가할 때 (ProjectAddCheckListRequest)
            if (requestDto instanceof ProjectAddCheckListRequest req) {
                List<Long> ids = req.getChecklistIds();
                if (ids != null && !ids.isEmpty()) {
                    String firstContent = checkListRepository.findById(ids.get(0))
                            .map(cl -> cl.getContent()).orElse("알 수 없는 항목");
                    String finalContent = ids.size() > 1 ? firstContent + " 외 " + (ids.size() - 1) + "건" : firstContent;
                    data.put("content", finalContent);
                }
            }
            // Case B: 새로운 체크리스트 이름을 직접 써서 생성할 때 (ProjectCreateCheckListRequest)
            else if (requestDto instanceof ProjectCreateCheckListRequest req) {
                // DTO에 이미 content('쳌생성')가 있으므로 바로 사용!
                data.put("content", req.getContent());
            }

            return toJson(data); // 처리가 끝났으므로 즉시 반환
        }

        // 3. 게시글 승인/거절 (메서드명 기반)
        if (lowerMethodName.contains("approve")) {
            data.put("action", "approved");
            data.put("content", oldContent);
            return toJson(data);
        }
        if (lowerMethodName.contains("reject")) {
            data.put("action", "rejected");
            data.put("content", oldContent);
            Object requestDto = findDtoFromArgs(joinPoint.getArgs());
            if (requestDto != null) {
                try {
                    JsonNode node = objectMapper.valueToTree(requestDto);
                    if (node.has("rejectReason")) {
                        data.put("rejectReason", node.get("rejectReason").asText());
                    }
                } catch (Exception e) { /* */ }
            }
            return toJson(data);
        }

        // 4. 일반 DTO 처리 (프로젝트 수정, 게시글 작성 등)
        Object requestDto = findDtoFromArgs(joinPoint.getArgs());
        if (requestDto == null) {
            if (!oldContent.isEmpty()) {
                data.put("content", oldContent);
                return toJson(data);
            }
            return null;
        }

        try {
            JsonNode node = objectMapper.valueToTree(requestDto);

            // 프로젝트 단계(STAGE) 변경 체크
            if ("Project".equals(targetType) && node.has("stageId") && !node.get("stageId").isNull()) {
                Long newStageId = node.get("stageId").asLong();
                String oldStageName = projectRepository.findCurrentStageNameByProjectId(targetId);
                String newStageName = stageRepository.findById(newStageId)
                        .map(Stage::getStageName).orElse("알 수 없음");

                data.put("type", "STAGE");
                data.put("old", oldStageName != null ? oldStageName : "미정");
                data.put("new", newStageName);
                return toJson(data);
            }

            // 일반적인 제목/내용 수정 또는 생성 처리
            String currentContent = extractTitleFromDto(requestDto);
            if ("UPDATE".equals(action)) {
                // 내용이 비어있으면(다른 필드만 수정 시) 기존 제목 유지
                if (currentContent == null || currentContent.trim().isEmpty()) {
                    data.put("content", oldContent);
                } else {
                    data.put("old", oldContent);
                    data.put("new", currentContent);
                }
            } else {
                data.put("content", currentContent);
            }
            return toJson(data);

        } catch (Exception e) {
            log.error("Detail 추출 실패: {}", e.getMessage());
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
                case "CheckList" -> checkListRepository.findCheckListNameById(targetId);
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

        // 3. 역추적 로직
        if (targetId != null) {
            // 체크리스트 역추적
            if ("CheckList".equals(targetType)) {
                return projectCheckListRepository.findById(targetId)
                        .map(pcl -> pcl.getProject().getId()).orElse(null);
            }
            // 게시글 역추적
            if ("Post".equals(targetType)) {
                return postRepository.findById(targetId)
                        .map(p -> p.getProject().getId()).orElse(null);
            }
            // 댓글 역추적
            if ("Comment".equals(targetType)) {
                return commentRepository.findProjectIdByCommentId(targetId);
            }
        }

        return null; // 모든 시도 후 실패 시 null 반환
    }

    private Long findTargetId(JoinPoint joinPoint, Object result, String targetType) {
        // 파라미터에서 먼저 찾기 (수정/삭제 시 가장 확실함)
        String paramName = TARGET_TYPE_PARAM_MAP.get(targetType);
        Long idFromParam = extractIdFromArgs(joinPoint, paramName);
        if (idFromParam != null) return idFromParam;

        // 2. 만약 projectId라는 이름의 인자가 있다면 반환
        Long projectId = extractIdFromArgs(joinPoint, "projectId");
        if (projectId != null) return projectId;

        // 2. Response에서 찾기
        Object unwrapped = unwrapCommonResponse(result);

        if (unwrapped instanceof ProjectCreateResponse res) return res.getProjectId();
        if (unwrapped instanceof PostCreateResponse res) return res.getPostId();
        if (unwrapped instanceof CommentResponse res) return res.getCommentId();
        if (unwrapped instanceof CheckListResponse res) return res.getCheckListId();

        // 3. ProjectMember는 Integer 반환하므로 여기서 처리 불가
        // -> 파라미터의 userId를 targetId로 사용
        log.warn("TargetId를 찾을 수 없음: targetType={}, resultType={}",
                targetType, unwrapped != null ? unwrapped.getClass().getSimpleName() : "null");

        return null;
    }

    private String extractTitleFromDto(Object dto) {
        try {
            JsonNode node = objectMapper.valueToTree(dto);

            // 승인 DTO는 제목이 없으므로 빈 문자열 반환
            if (node.has("approverId") || node.has("replierId") || node.has("replitUserId")) {
                return "";}
            if (node.has("projectName")) return node.get("projectName").asText();
            if (node.has("title")) return node.get("title").asText();
            if (node.has("content")) return node.get("content").asText();
            if (node.has("name")) return node.get("name").asText();
            if (node.has("checkListName")) return node.get("checkListName").asText();
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
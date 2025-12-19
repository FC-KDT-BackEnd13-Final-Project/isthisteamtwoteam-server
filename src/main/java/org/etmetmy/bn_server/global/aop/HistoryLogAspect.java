package org.etmetmy.bn_server.global.aop;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.comment.repository.CommentRepository;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;
import org.etmetmy.bn_server.domain.history.event.HistoryCommentEvent;
import org.etmetmy.bn_server.domain.history.event.HistoryFileEvent;
import org.etmetmy.bn_server.domain.history.event.HistoryLinkEvent;
import org.etmetmy.bn_server.domain.history.event.HistoryPostEvent;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.link.repository.LinkRepository;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BoardNotFoundException;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.global.util.IpAddressUtil;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// 2. Aspect 클래스 생성
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class HistoryLogAspect {
    private final ApplicationEventPublisher eventPublisher;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final FileRepository fileRepository;
    private final LinkRepository linkRepository;
    private final EntityManager entityManager;

    @Pointcut("@annotation(org.etmetmy.bn_server.global.aop.HistoryLogger)")
    public void historyLoggerPointcut() {
    }

    // @Around 사용 - 변경 전 데이터 캡처 가능
    @Around("historyLoggerPointcut() && @annotation(historyLogger)")
    public Object saveHistoryLog(ProceedingJoinPoint joinPoint, HistoryLogger historyLogger)
            throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();
        Long userId = SessionUtil.getLoginUserId(request.getSession());
        String clientIp = IpAddressUtil.getClientIp(request);

        // targetType에 따라 분기 처리
        Object result;
        switch (historyLogger.targetType()) {
            case "Post" -> result = handlePostHistory(joinPoint, historyLogger, userId, clientIp);
            case "Comment" -> result = handleCommentHistory(joinPoint, historyLogger, userId, clientIp);
            case "File" -> result = handleFileHistory(joinPoint, historyLogger, userId, clientIp);
            case "Link" -> result = handleLinkHistory(joinPoint, historyLogger, userId, clientIp);
            default -> throw new IllegalArgumentException("지원하지 않는 targetType: " + historyLogger.targetType());
        }

        return result;
    }

    // ==================== Post History ====================
    private Object handlePostHistory(ProceedingJoinPoint joinPoint, HistoryLogger historyLogger,
                                     Long userId, String clientIp) throws Throwable {
        // 1. 변경 전 데이터 조회 및 스냅샷 생성
        Long postId = extractPostId(joinPoint);
        Post originalPost = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 변경 전 데이터를 스냅샷으로 복사
        Post originalSnapshot = createPostSnapshot(originalPost);
        entityManager.detach(originalSnapshot);

        // 2. 메서드 실행 (실제 수정/삭제 작업)
        Object result = joinPoint.proceed();

        // 3. 변경 후 데이터 생성 (UPDATE인 경우만)
        Post updatedPost = null;
        if (historyLogger.changeType() == ChangeType.UPDATE) {
            updatedPost = createPostSnapshot(originalPost);
        }

        // 4. 히스토리 이벤트 발행
        eventPublisher.publishEvent(
                new HistoryPostEvent(originalSnapshot, updatedPost, historyLogger.changeType(), userId, clientIp)
        );

        return result;
    }

    // ==================== Comment History ====================
    private Object handleCommentHistory(ProceedingJoinPoint joinPoint, HistoryLogger historyLogger,
                                        Long userId, String clientIp) throws Throwable {
        // 1. 변경 전 데이터 조회 및 스냅샷 생성
        Long commentId = extractCommentId(joinPoint);
        Comment originalComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_COMMENT_NOT_FOUND));

        // 변경 전 데이터를 스냅샷으로 복사
        Comment originalSnapshot = createCommentSnapshot(originalComment);
        entityManager.detach(originalSnapshot);

        // 2. 메서드 실행 (실제 수정/삭제 작업)
        Object result = joinPoint.proceed();

        // 3. 변경 후 데이터 생성 (UPDATE인 경우만)
        Comment updatedComment = null;
        if (historyLogger.changeType() == ChangeType.UPDATE) {
            updatedComment = createCommentSnapshot(originalComment);
        }

        // 4. 히스토리 이벤트 발행
        eventPublisher.publishEvent(
                new HistoryCommentEvent(originalSnapshot, updatedComment, historyLogger.changeType(), userId, clientIp)
        );

        return result;
    }

    // ==================== File History ====================
    private Object handleFileHistory(ProceedingJoinPoint joinPoint, HistoryLogger historyLogger,
                                     Long userId, String clientIp) throws Throwable {
        // File은 CREATE, DELETE만 지원
        Long fileId = extractFileId(joinPoint);
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        // 메서드 실행
        Object result = joinPoint.proceed();

        // 히스토리 이벤트 발행
        eventPublisher.publishEvent(
                new HistoryFileEvent(file, historyLogger.changeType(), userId, clientIp)
        );

        return result;
    }

    // ==================== Link History ====================
    private Object handleLinkHistory(ProceedingJoinPoint joinPoint, HistoryLogger historyLogger,
                                     Long userId, String clientIp) throws Throwable {
        // Link는 CREATE, DELETE만 지원
        Long linkId = extractLinkId(joinPoint);
        Link link = linkRepository.findById(linkId)
                .orElseThrow(()->new BusinessException(ErrorCode.LINK_NOT_FOUND));

        // 메서드 실행
        Object result = joinPoint.proceed();

        // 히스토리 이벤트 발행
        eventPublisher.publishEvent(
                new HistoryLinkEvent(link, historyLogger.changeType(), userId, clientIp)
        );

        return result;
    }

    // ==================== Snapshot Creation ====================

    /**
     * Post 엔티티의 스냅샷을 생성합니다.
     * LazyInitializationException 방지를 위해 프록시 객체를 즉시 로딩합니다.
     */
    private Post createPostSnapshot(Post original) {
        // Lazy 프록시 강제 초기화
        if (original.getStage() != null) {
            original.getStage().getStageName();
        }
        if (original.getProject() != null) {
            original.getProject().getProjectName();
        }
        if (original.getUser() != null) {
            original.getUser().getName();
        }

        return Post.builder()
                .postId(original.getPostId())
                .project(original.getProject())
                .user(original.getUser())
                .parentPostId(original.getParentPostId())
                .title(original.getTitle())
                .content(original.getContent())
                .isCompleted(original.getIsCompleted())
                .createdIp(original.getCreatedIp())
                .stage(original.getStage())
                .postNumber(original.getPostNumber())
                .isDeleted(original.getIsDeleted())
                .deletedAt(original.getDeletedAt())
                .deletedBy(original.getDeletedBy())
                .build();
    }

    /**
     * Comment 엔티티의 스냅샷을 생성합니다.
     * LazyInitializationException 방지를 위해 프록시 객체를 즉시 로딩합니다.
     */
    private Comment createCommentSnapshot(Comment original) {
        // Lazy 프록시 강제 초기화
        if (original.getPost() != null) {
            original.getPost().getTitle();
        }
        if (original.getUser() != null) {
            original.getUser().getName();
        }
        if (original.getParent() != null) {
            original.getParent().getContent();
        }

        return Comment.builder()
                .commentId(original.getCommentId())
                .post(original.getPost())
                .user(original.getUser())
                .content(original.getContent())
                .ip(original.getIp())
                .parent(original.getParent())
                .isDeleted(original.getIsDeleted())
                .deletedAt(original.getDeletedAt())
                .deletedBy(original.getDeletedBy())
                .build();
    }

    // ==================== ID Extraction ====================

    /**
     * JoinPoint에서 postId를 추출합니다.
     */
    private Long extractPostId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        throw new IllegalArgumentException("postId를 찾을 수 없습니다. @PathVariable Long이 필요합니다.");
    }

    /**
     * JoinPoint에서 commentId를 추출합니다.
     */
    private Long extractCommentId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        throw new IllegalArgumentException("commentId를 찾을 수 없습니다. @PathVariable Long이 필요합니다.");
    }

    /**
     * JoinPoint에서 fileId를 추출합니다.
     */
    private Long extractFileId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        throw new IllegalArgumentException("fileId를 찾을 수 없습니다. @PathVariable Long이 필요합니다.");
    }

    /**
     * JoinPoint에서 linkId를 추출합니다.
     */
    private Long extractLinkId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        throw new IllegalArgumentException("linkId를 찾을 수 없습니다. @PathVariable Long이 필요합니다.");
    }
}

package org.etmetmy.bn_server.domain.history.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;
import org.etmetmy.bn_server.domain.history.entity.HistoryComment;
import org.etmetmy.bn_server.domain.history.event.HistoryCommentEvent;
import org.etmetmy.bn_server.domain.history.repository.HistoryCommentRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class HistoryCommentEventListener {

    private final HistoryCommentRepository historyCommentRepository;
    private final UserRepository userRepository;

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleHistoryCommentEvent(HistoryCommentEvent event) {
        try {
            log.info("Processing HistoryCommentEvent - commentId: {}, changeType: {}",
                    event.getOriginalComment().getCommentId(),
                    event.getChangeType());

            User changedByUser = userRepository.findById(event.getChangedByUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + event.getChangedByUserId()));

            HistoryComment historyComment = createHistoryComment(
                    event.getOriginalComment(),
                    event.getUpdatedComment(),
                    event.getChangeType(),
                    changedByUser,
                    event.getChangeIp()
            );

            historyCommentRepository.save(historyComment);

            log.info("HistoryComment saved successfully - historyCommentId: {}", historyComment.getHistoryCommentId());

        } catch (Exception e) {
            log.error("Failed to save HistoryComment - commentId: {}, error: {}",
                    event.getOriginalComment().getCommentId(),
                    e.getMessage(), e);
        }
    }

    private HistoryComment createHistoryComment(
            Comment originalComment,
            Comment updatedComment,
            ChangeType changeType,
            User changedByUser,
            String changeIp
    ) {
        HistoryComment.HistoryCommentBuilder builder = HistoryComment.builder()
                .originalCommentId(originalComment.getCommentId())
                .changeType(changeType)
                .changedByUser(changedByUser)
                .changeIp(changeIp)
                .postId(originalComment.getPost() != null ? originalComment.getPost().getPostId() : null)
                .projectId(originalComment.getPost() != null && originalComment.getPost().getProject() != null
                        ? originalComment.getPost().getProject().getId() : null);

        // 변경 전 (Before) 데이터 설정
        builder.beContent(originalComment.getContent());

        // 변경 후 (After) 데이터 설정 (UPDATE인 경우만)
        if (changeType == ChangeType.UPDATE && updatedComment != null) {
            builder.afContent(updatedComment.getContent());
        }

        return builder.build();
    }
}

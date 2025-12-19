package org.etmetmy.bn_server.domain.history.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.history.entity.HistoryLink;
import org.etmetmy.bn_server.domain.history.event.HistoryLinkEvent;
import org.etmetmy.bn_server.domain.history.repository.HistoryLinkRepository;
import org.etmetmy.bn_server.domain.link.entity.Link;
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
public class HistoryLinkEventListener {

    private final HistoryLinkRepository historyLinkRepository;
    private final UserRepository userRepository;

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleHistoryLinkEvent(HistoryLinkEvent event) {
        try {
            log.info("Processing HistoryLinkEvent - linkId: {}, changeType: {}",
                    event.getLink().getLinkId(),
                    event.getChangeType());

            User changedByUser = userRepository.findById(event.getChangedByUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + event.getChangedByUserId()));

            HistoryLink historyLink = createHistoryLink(
                    event.getLink(),
                    event.getChangeType(),
                    changedByUser,
                    event.getChangeIp()
            );

            historyLinkRepository.save(historyLink);

            log.info("HistoryLink saved successfully - historyLinkId: {}", historyLink.getHistoryLinkId());

        } catch (Exception e) {
            log.error("Failed to save HistoryLink - linkId: {}, error: {}",
                    event.getLink().getLinkId(),
                    e.getMessage(), e);
        }
    }

    private HistoryLink createHistoryLink(
            Link link,
            org.etmetmy.bn_server.domain.history.entity.ChangeType changeType,
            User changedByUser,
            String changeIp
    ) {
        return HistoryLink.builder()
                .originalLinkId(link.getLinkId())
                .changeType(changeType)
                .linkUrl(link.getLinkUrl())
                .changedByUser(changedByUser)
                .changeIp(changeIp)
                .postId(link.getPost() != null ? link.getPost().getPostId() : null)
                .commentId(link.getComment() != null ? link.getComment().getCommentId() : null)
                .projectCheckListId(link.getProjectCheckList() != null ? link.getProjectCheckList().getProjectCheckListId() : null)
                .projectId(link.getPost() != null && link.getPost().getProject() != null
                        ? link.getPost().getProject().getId() : null)
                .build();
    }
}

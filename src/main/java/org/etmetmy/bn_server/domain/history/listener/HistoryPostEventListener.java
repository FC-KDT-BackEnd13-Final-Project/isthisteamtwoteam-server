package org.etmetmy.bn_server.domain.history.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;
import org.etmetmy.bn_server.domain.history.entity.HistoryPost;
import org.etmetmy.bn_server.domain.history.event.HistoryPostEvent;
import org.etmetmy.bn_server.domain.history.repository.HistoryPostRepository;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * HistoryPostEvent를 수신하여 게시글 변경 이력을 저장합니다.
 * 비동기 처리로 메인 트랜잭션에 영향을 주지 않습니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HistoryPostEventListener {

    private final HistoryPostRepository historyPostRepository;
    private final UserRepository userRepository;

    /**
     * 게시글 변경 이력 이벤트를 처리합니다.
     * @Async: 비동기로 처리하여 응답 시간에 영향을 주지 않습니다.
     * @Transactional(propagation = REQUIRES_NEW): 별도 트랜잭션으로 처리하여 메인 작업과 분리합니다.
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleHistoryPostEvent(HistoryPostEvent event) {
        try {
            log.info("Processing HistoryPostEvent - postId: {}, changeType: {}",
                    event.getOriginalPost().getPostId(),
                    event.getChangeType());

            // User 조회
            User changedByUser = userRepository.findById(event.getChangedByUserId())
                    .orElseThrow(UserNotFoundException::new);

            // HistoryPost 엔티티 생성
            HistoryPost historyPost = createHistoryPost(
                    event.getOriginalPost(),
                    event.getUpdatedPost(),
                    event.getChangeType(),
                    changedByUser,
                    event.getChangeIp()
            );

            // 저장
            historyPostRepository.save(historyPost);

            log.info("HistoryPost saved successfully - historyPostId: {}", historyPost.getHistoryPostId());

        } catch (Exception e) {
            log.error("Failed to save HistoryPost - postId: {}, error: {}",
                    event.getOriginalPost().getPostId(),
                    e.getMessage(), e);
            // 히스토리 저장 실패가 메인 트랜잭션에 영향을 주지 않도록 예외를 먹습니다
        }
    }

    /**
     * HistoryPost 엔티티를 생성합니다.
     */
    private HistoryPost createHistoryPost(
            Post originalPost,
            Post updatedPost,
            ChangeType changeType,
            User changedByUser,
            String changeIp
    ) {
        HistoryPost.HistoryPostBuilder builder = HistoryPost.builder()
                .originalPostId(originalPost.getPostId())
                .changeType(changeType)
                .changedByUser(changedByUser)
                .changeIp(changeIp)
                .projectId(originalPost.getProject() != null ? originalPost.getProject().getId() : null);

        // 변경 전 (Before) 데이터 설정
        builder.beTitle(originalPost.getTitle())
                .beContent(originalPost.getContent())
                .beStageId(originalPost.getStage() != null ? originalPost.getStage().getId() : null)
                .beStageName(originalPost.getStage() != null ? originalPost.getStage().getStageName() : null)
                .beIsCompleted(originalPost.getIsCompleted());

        // 변경 후 (After) 데이터 설정 (UPDATE인 경우만)
        if (changeType == ChangeType.UPDATE && updatedPost != null) {
            builder.afTitle(updatedPost.getTitle())
                    .afContent(updatedPost.getContent())
                    .afStageId(updatedPost.getStage() != null ? updatedPost.getStage().getId() : null)
                    .afStageName(updatedPost.getStage() != null ? updatedPost.getStage().getStageName() : null)
                    .afIsCompleted(updatedPost.getIsCompleted());
        }

        return builder.build();
    }
}

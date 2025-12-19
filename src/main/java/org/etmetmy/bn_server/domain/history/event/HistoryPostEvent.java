package org.etmetmy.bn_server.domain.history.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;
import org.etmetmy.bn_server.domain.post.entity.Post;

/**
 * 게시글 변경 이력 이벤트
 * AOP에서 발행되어 EventListener에서 처리됩니다.
 */
@Getter
@AllArgsConstructor
public class HistoryPostEvent {

    private final Post originalPost;      // 변경 전 데이터
    private final Post updatedPost;       // 변경 후 데이터 (DELETE시 null)
    private final ChangeType changeType;  // UPDATE, DELETE
    private final Long changedByUserId;   // 변경한 사용자 ID
    private final String changeIp;        // 변경 IP 주소
}

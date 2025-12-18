package org.etmetmy.bn_server.domain.history.event;

import lombok.Getter;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;

@Getter
public class HistoryCommentEvent {

    private final Comment originalComment;    // 변경 전 데이터
    private final Comment updatedComment;     // 변경 후 데이터 (DELETE시 null)
    private final ChangeType changeType;      // UPDATE, DELETE
    private final Long changedByUserId;       // 변경한 사용자 ID
    private final String changeIp;            // 변경 IP 주소

    public HistoryCommentEvent(
            Comment originalComment,
            Comment updatedComment,
            ChangeType changeType,
            Long changedByUserId,
            String changeIp
    ) {
        this.originalComment = originalComment;
        this.updatedComment = updatedComment;
        this.changeType = changeType;
        this.changedByUserId = changedByUserId;
        this.changeIp = changeIp;
    }
}

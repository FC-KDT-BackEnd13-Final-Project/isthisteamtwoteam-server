package org.etmetmy.bn_server.domain.history.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;

@Getter
@AllArgsConstructor
public class HistoryCommentEvent {

    private final Comment originalComment;    // 변경 전 데이터
    private final Comment updatedComment;     // 변경 후 데이터 (DELETE시 null)
    private final ChangeType changeType;      // UPDATE, DELETE
    private final Long changedByUserId;       // 변경한 사용자 ID
    private final String changeIp;            // 변경 IP 주소
}

package org.etmetmy.bn_server.domain.history.event;

import lombok.Getter;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;
import org.etmetmy.bn_server.domain.link.entity.Link;

@Getter
public class HistoryLinkEvent {

    private final Link link;                  // 링크 정보
    private final ChangeType changeType;      // CREATE, DELETE
    private final Long changedByUserId;       // 변경한 사용자 ID
    private final String changeIp;            // 변경 IP 주소

    public HistoryLinkEvent(
            Link link,
            ChangeType changeType,
            Long changedByUserId,
            String changeIp
    ) {
        this.link = link;
        this.changeType = changeType;
        this.changedByUserId = changedByUserId;
        this.changeIp = changeIp;
    }
}

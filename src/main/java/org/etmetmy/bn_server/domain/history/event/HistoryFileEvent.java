package org.etmetmy.bn_server.domain.history.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;

@Getter
@AllArgsConstructor
public class HistoryFileEvent {

    private final File file;                  // 파일 정보
    private final ChangeType changeType;      // CREATE, DELETE
    private final Long changedByUserId;       // 변경한 사용자 ID
    private final String changeIp;            // 변경 IP 주소
}

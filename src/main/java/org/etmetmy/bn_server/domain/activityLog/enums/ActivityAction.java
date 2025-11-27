package org.etmetmy.bn_server.domain.activityLog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityAction {

    CREATE("생성"),
    UPDATE("수정"),
    DELETE("삭제"),

    // 프로젝트 관리용
    ASSIGN("담당자 배정"),
    UNASSIGN("담당자 해제"),
    STATUS_CHANGE("상태 변경"),
    COMMENT_ADD("댓글 작성");

    private final String description;
}

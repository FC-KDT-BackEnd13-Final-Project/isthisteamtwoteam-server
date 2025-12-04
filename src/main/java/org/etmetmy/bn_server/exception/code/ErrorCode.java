package org.etmetmy.bn_server.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE(400, "C002", "잘못된 타입입니다."),
    INVALID_NOT_ALLOWED(405, "C003", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(500, "C004", "서버 오류가 발생했습니다."),
    ENTITY_NOT_FOUND(404, "C005", "요청한 리소스를 찾을 수 없습니다."),

    PROJECT_NOT_FOUND(404, "P001", "프로젝트를 찾을 수 없습니다."),
    PROJECT_ALREADY_EXISTS(409, "P002", "이미 존재하는 프로젝트입니다."),  // ← 수정!
    PROJECT_PERMISSION_DENIED(403, "P003", "프로젝트 접근 권한이 없습니다."),
    PROJECT_CANNOT_DELETE(400, "P004", "진행 중인 프로젝트는 삭제할 수 없습니다."),
    PROJECT_NAME_DUPLICATE(409, "P005", "중복된 프로젝트명입니다."),

    BOARD_POST_NOT_FOUND(404, "B001", "게시글을 찾을 수 없습니다."),
    BOARD_COMMENT_NOT_FOUND(404, "B002", "댓글을 찾을 수 없습니다."),  // ← 수정!
    BOARD_PERMISSION_DENIED(403, "B003", "게시글 수정/삭제 권한이 없습니다."),
    BOARD_ALREADY_DELETED(400, "B004", "이미 삭제된 게시글입니다."),

    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(409, "U002", "이미 사용 중인 이메일입니다."),
    USER_ALREADY_DELETED(400, "U003", "이미 탈퇴한 사용자입니다."),

    UNAUTHORIZED(401, "A001", "인증이 필요합니다."),
    FORBIDDEN(403, "A002", "접근 권한이 없습니다."),
    INVALID_TOKEN(401, "A003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A004", "만료된 토큰입니다."),
    INVALID_PASSWORD(401, "A005", "비밀번호가 일치하지 않습니다."),

    FILE_NOT_FOUND(404, "F001", "파일을 찾을 수 없습니다"),
    FILE_SIZE_EXCEEDED(400, "F002", "파일 크기가 제한을 초과했습니다"),
    INVALID_FILE_TYPE(400, "F003", "지원하지 않는 파일 형식입니다"),
    FILE_UPLOAD_FAILED(500, "F004", "파일 업로드에 실패했습니다"),

    // 체크리스트 관련 에러
    CHECKLIST_NOT_FOUND(404,"D001","체크리스트를 찾을 수 없습니다."),

    //단계 관련 에러
    STAGE_NOT_FOUND(404, "S001", "단계를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

}

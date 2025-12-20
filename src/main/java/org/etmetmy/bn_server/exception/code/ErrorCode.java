package org.etmetmy.bn_server.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증 관련 에러
    UNAUTHORIZED(401, "A001", "인증이 필요합니다."),
    FORBIDDEN(403, "A002", "접근 권한이 없습니다."),
    INVALID_TOKEN(401, "A003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A004", "만료된 토큰입니다."),
    INVALID_PASSWORD(401, "A005", "비밀번호가 일치하지 않습니다."),

    RESET_CODE_NOT_FOUND(400, "A006", "비밀번호 재설정 인증코드를 찾을 수 없습니다."),
    RESET_CODE_ALREADY_USED(400, "A007", "이미 사용된 인증코드입니다."),
    EXPIRED_RESET_CODE(400, "A008", "만료된 인증코드입니다."),
    INVALID_RESET_CODE(400, "A009", "유효하지 않은 인증코드입니다."),

    // 게시글 관련 에러
    BOARD_POST_NOT_FOUND(404, "B001", "게시글을 찾을 수 없습니다."),
    BOARD_PERMISSION_DENIED(403, "B003", "게시글 수정/삭제 권한이 없습니다."),
    BOARD_ALREADY_DELETED(400, "B006", "이미 삭제된 게시글입니다."),
    BOARD_NOT_DELETED(400, "B005", "삭제되지 않은 게시글은 복구할 수 없습니다."),
    BOARD_INVALID_FILTER(400,"B005", "유효하지 않은 게시글 필터입니다."),
    POST_PROJECT_MISMATCH(400, "P014", "게시글이 해당 프로젝트에 속하지 않습니다."),
    DELETED_BOARD_ACCESS_DENIED(403,"D006","삭제된 게시글에 접근 권한이 없습니다."),
    REQUEST_PERMISSION_DENIED(403, "R003", "요청 승인/거절 권한이 없습니다."),

    // 댓글 관련 에러
    BOARD_COMMENT_NOT_FOUND(404, "C001", "댓글을 찾을 수 없습니다."),
    INVALID_PARENT_COMMENT(400, "C002", "유효하지 않은 부모 댓글입니다."),
    PARENT_COMMENT_NOT_FOUND(404, "C003", "부모 댓글을 찾을 수 없습니다."),
    COMMENT_PERMISSION_DENIED(403,"C004","댓글 수정/삭제 권한이 없습니다."),
    COMMENT_ALREADY_DELETED(400, "B002_3", "이미 삭제된 댓글입니다."),

    //공통 에러
    INVALID_INPUT_VALUE(400, "I001", "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE(400, "I002", "잘못된 타입입니다."),
    INVALID_NOT_ALLOWED(405, "I003", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(500, "I004", "서버 오류가 발생했습니다."),
    ENTITY_NOT_FOUND(404, "E005", "요청한 리소스를 찾을 수 없습니다."),

    // 프로젝트 관련 에러
    PROJECT_NOT_FOUND(404, "P001", "프로젝트를 찾을 수 없습니다."),
    PROJECT_ALREADY_EXISTS(409, "P002", "이미 존재하는 프로젝트입니다."),
    PROJECT_PERMISSION_DENIED(403, "P003", "프로젝트 접근 권한이 없습니다."),
    PROJECT_CANNOT_DELETE(400, "P004", "진행 중인 프로젝트는 삭제할 수 없습니다."),
    PROJECT_NAME_DUPLICATE(409, "P005", "중복된 프로젝트명입니다."),
    PROJECT_AND_USER_NOT_FOUND(404,"PU001", "사용자가 해당 프로젝트에 속해있지 않습니다."),
    DELETED_PROJECT_ACCESS_DENIED(403, "P006", "삭제된 프로젝트 접근 권한이 없습니다."),
    PROJECT_NOT_DELETED(400, "P007", "이미 삭제되지 않은 프로젝트입니다."),
    PROJECT_CHECKLIST_NOT_FOUND(404,"P008","프로젝트 체크리스트가 존재하지 않습니다."),

    // 승인 관련 에러
    REQUEST_PENDING_NOT_FOUND(404,"R001","해당 게시글에 승인 대기 중인 요청이 없습니다."),

    // 유저 관련 에러
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(409, "U002", "이미 사용 중인 이메일입니다."),
    USER_ALREADY_DELETED(400, "U003", "이미 탈퇴한 사용자입니다."),
    USER_NOT_LOGIN(401,"Y004", "로그인이 필요합니다."),

    //파일 관련 에러
    FILE_NOT_FOUND(404, "F001", "파일을 찾을 수 없습니다"),
    FILE_SIZE_EXCEEDED(400, "F002", "파일 크기가 제한을 초과했습니다"),
    INVALID_FILE_TYPE(400, "F003", "지원하지 않는 파일 형식입니다"),
    FILE_UPLOAD_FAILED(500, "F004", "파일 업로드에 실패했습니다"),
    FILE_NOT_IN_POST(400, "F005", "파일이 해당 게시글에 속하지 않습니다."),
    INVALID_URL_FORMAT(400,"C005", "잘못된 URL 형식입니다."),
    FILE_DELETE_FAILED(500, "F006", "파일 삭제에 실패했습니다"),
    FILE_NOT_TEMP(400, "F007", "임시 파일이 아닙니다."),
    FILE_NOT_DELETED(400, "F008", "삭제된 파일만 영구 삭제할 수 있습니다."),
    DELETED_FILE_ACCESS_DENIED(403,"F009","삭제된 파일에 접근 권한이 없습니다."),

    // 체크리스트 관련 에러
    CHECKLIST_NOT_FOUND(404,"D001","체크리스트를 찾을 수 없습니다."),

    //단계 관련 에러
    STAGE_NOT_FOUND(404, "S001", "단계를 찾을 수 없습니다."),

    // 페이지 응답
    NON_INDEX_PAGE(404, "P007","페이지가 없습니다"),
    NON_SIZE_PAGE(404,"P008","데이터가 없습니다."),

    //memo 에러
    MEMO_NOT_FOUND(404,"M001","존재하지 않는 메모입니다."),

    // 링크 에러
    LINK_NOT_FOUND(404,"L001","존재하지 않는 링크입니다."),

    //회사 에러
    COMPANY_NOT_FOUND(404,"CMP001", "회사 정보가 존재하지 않습니다.");

    private final int status;
    private final String code;
    private final String message;

}

package org.etmetmy.bn_server.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StatusCode {

    // 성공 응답
    POST_CREATED(HttpStatus.CREATED, "게시글 생성이 잘 되었습니다."),
    POST_FOUND(HttpStatus.OK, "게시글 조회 완료"),

    // CheckList 성공 응답
    CHECKLISTS_FOUND(HttpStatus.OK, "체크리스트 조회 완료"),

    // 페이지 응답
    NON_INDEX_PAGE(HttpStatus.OK, "페이지가 없습니다"),
    NON_SIZE_PAGE(HttpStatus.OK,"데이터가 없습니다."),

    // 게시글 응답
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    REQUEST_PENDING_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 게시글에 승인 대기 중인 요청이 없습니다."),

    // 댓글 응답
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 부모 댓글 ID입니다."),


    //Company 에러
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회사입니다."),

    //memo 에러
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메모입니다."),

    //User 에서
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    USER_NOT_LOGIN(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    private final HttpStatus status;
    private final String message;
}

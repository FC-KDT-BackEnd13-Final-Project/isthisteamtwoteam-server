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

    // 에러 응답
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다.");

    private final HttpStatus status;
    private final String message;
}

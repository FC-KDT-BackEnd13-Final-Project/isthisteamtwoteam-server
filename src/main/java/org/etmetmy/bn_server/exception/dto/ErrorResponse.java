package org.etmetmy.bn_server.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code; //에러코드
    private final String message; //에러 메세지
    private final int status; //HTTP 상태코드
    private final LocalDateTime timestamp; //에러발생시간
    private final String path; //에러발생 API 경로
    private final List<FieldError> fieldErrors; //유효성 검사 실패 시 상세 정보
    private final String debugMessage; //디버그 정보

    @Getter
    @Builder
    public static class FieldError {

        private final String field; //에러발생 필드명
        private final String value; //잘못된 입력값
        private final String reason; //에러 이유

    }
}

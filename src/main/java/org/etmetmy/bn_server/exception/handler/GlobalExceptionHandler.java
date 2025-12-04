package org.etmetmy.bn_server.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.dto.ErrorResponse;
import org.etmetmy.bn_server.global.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    //todo: 커스텀 예외 모두 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handlerBusinessException(
            BusinessException e,
            HttpServletRequest request){

        log.warn("BusinessException: code={}, message={}, path={}, method={}",
                e.getErrorCode().getCode(),
                e.getMessage(),
                request.getRequestURI(),
                request.getMethod());

        ErrorResponse response = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .debugMessage(isDevelopmentMode() ? getStackTrace(e) : null)
                .build();

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(response);
    }

    //todo: @Valid 유효성 검사 실패 처리
    //DTO 유효성 검사 실패 시 동작
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handlerMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        log.warn("Validation failed: path={}, errors={}",
                request.getRequestURI(),
                e.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": "+error.getDefaultMessage())
                        .collect(Collectors.joining(", ")));

        //유효성 검사 실패 필드 정보 추출
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .value(error.getRejectedValue() != null ?
                                error.getRejectedValue().toString() : "")
                        .reason(error.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message(ErrorCode.INVALID_INPUT_VALUE.getMessage())
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .debugMessage(isDevelopmentMode() ? "Validation errors: " + fieldErrors.size() + " field(s)" : null)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    //todo: 모든 예외 최종 처리(fallback)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handlerException(
            Exception e,
            HttpServletRequest request) {

        log.error("Unexpected error: {} at {} [{}]",
                e.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod(),
                e);

        ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .debugMessage(isDevelopmentMode() ? getStackTrace(e) : null)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * 개발 환경 여부 확인
     * dev, local, default 프로파일일 때 true 반환
     */
    private boolean isDevelopmentMode() {
        return !activeProfile.equals("prod");
    }

    /**
     * 예외 스택 트레이스를 문자열로 변환
     */
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}


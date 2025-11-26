package org.etmetmy.bn_server.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //todo: 커스텀 예외 모두 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handlerBusinessException(
            BusinessException e,
            HttpServletRequest request){

        log.error("BusinessException: code={}, message={}, path={}",
                e.getErrorCode().getCode(),
                e.getMessage(),
                request.getRequestURL());

        ErrorResponse response = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
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

        log.error("Validation error: {}", e.getMessage());

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

        log.error("Unexpected error occurred", e);

        ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}


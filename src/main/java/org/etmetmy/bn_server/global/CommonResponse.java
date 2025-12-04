package org.etmetmy.bn_server.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {
    private final Boolean success;
    private final T response;
    private final String message;

    @Builder
    private CommonResponse(Boolean success, T response, String message) {
        this.success = success;
        this.response = response;
        this.message = message;
    }

    // 성공 + 데이터 + 메시지
    public static <T> CommonResponse<T> success(String message, T response) {
        return CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .response(response)
                .build();
    }

    // 성공 + 메시지만
    public static CommonResponse<Object> success(String message) {
        return CommonResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    // 실패 + 메시지
    public static CommonResponse<Object> fail(String message) {
        return CommonResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}

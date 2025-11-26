package org.etmetmy.bn_server.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String message;

    @Builder
    private ErrorResponse(String message) {
        this.message = message;
    }
}
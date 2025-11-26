package org.etmetmy.bn_server.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    private final StatusCode statusCode;
}

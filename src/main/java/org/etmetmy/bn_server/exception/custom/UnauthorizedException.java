package org.etmetmy.bn_server.exception.custom;

import org.etmetmy.bn_server.exception.code.ErrorCode;

//todo: 인증이 필요할 때 발생하는 예외
public class UnauthorizedException extends BusinessException{

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}

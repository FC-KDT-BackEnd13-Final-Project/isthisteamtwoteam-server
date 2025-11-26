package org.etmetmy.bn_server.exception.custom;

import org.etmetmy.bn_server.exception.code.ErrorCode;

//todo: 잘못된 입력값이 들어왔을 때 발생하는 예외
public class InvalidInputException extends BusinessException {

    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT_VALUE);
    }

    public InvalidInputException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}

package org.etmetmy.bn_server.exception.custom;

import org.etmetmy.bn_server.exception.code.ErrorCode;

//todo: 사용자를 찾을 수 없을 때 발생하는 예외
public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(){
        super(ErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(String message){
        super(ErrorCode.USER_NOT_FOUND, message);
    }
}

package org.etmetmy.bn_server.exception.custom;

import lombok.Getter;
import org.etmetmy.bn_server.exception.code.ErrorCode;

//todo: 비즈니스 로직에서 발생하는 최상위 예외
/*모든 커스텀 예외는 이 클래스를 상속받는다.*/

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }
}

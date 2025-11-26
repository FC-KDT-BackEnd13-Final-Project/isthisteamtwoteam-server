package org.etmetmy.bn_server.exception.custom;

import org.etmetmy.bn_server.exception.code.ErrorCode;

//todo: 프로젝트를 찾을 수 없을 때 발생하는 예외
public class ProjectNotFoundException extends BusinessException {

    public ProjectNotFoundException(ErrorCode errorCode){
        super(ErrorCode.PROJECT_NOT_FOUND);
    }

    public ProjectNotFoundException(String message){
        super(ErrorCode.PROJECT_NOT_FOUND, message);
    }
}

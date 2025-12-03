package org.etmetmy.bn_server.exception.custom;

import org.etmetmy.bn_server.exception.code.ErrorCode;

//todo: 프로젝트 접근 권한이 없을 때 발생하는 예외
public class ProjectPermissionDeniedException extends BusinessException {

    public ProjectPermissionDeniedException() {
        super(ErrorCode.PROJECT_PERMISSION_DENIED);
    }

    public ProjectPermissionDeniedException(String message) {
        super(ErrorCode.PROJECT_PERMISSION_DENIED, message);
    }
}
package org.etmetmy.bn_server.exception.custom;

import org.etmetmy.bn_server.exception.code.ErrorCode;

//todo: 게시글을 찾을 수 없을 때 발생하는 예외
public class BoardNotFoundException extends BusinessException {

    public BoardNotFoundException(){
        super(ErrorCode.BOARD_POST_NOT_FOUND);
    }

    public BoardNotFoundException(String message){
        super(ErrorCode.BOARD_POST_NOT_FOUND, message);
    }
}
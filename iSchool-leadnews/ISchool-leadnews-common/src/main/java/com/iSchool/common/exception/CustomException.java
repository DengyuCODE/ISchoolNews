package com.iSchool.common.exception;

import com.iSchool.model.common.enums.AppHttpCodeEnum;

public class CustomException extends RuntimeException {

    private AppHttpCodeEnum appHttpCodeEnum;

    private String msg;

    public CustomException(AppHttpCodeEnum appHttpCodeEnum){
        this.appHttpCodeEnum = appHttpCodeEnum;
    }

    public CustomException(AppHttpCodeEnum appHttpCodeEnum, String message){
        this.appHttpCodeEnum = appHttpCodeEnum;
        msg = message;
    }

    public AppHttpCodeEnum getAppHttpCodeEnum() {
        return appHttpCodeEnum;
    }
}

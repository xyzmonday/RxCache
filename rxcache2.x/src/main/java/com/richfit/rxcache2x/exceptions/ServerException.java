package com.richfit.rxcache2x.exceptions;

import java.lang.Exception;

/**
 * 自定义异常类-api接口调用 服务器操作异常
 * Created by yubaokang on 2016/9/13.
 */
public class ServerException extends Exception {

    private String returnCode;

    /**
     * Constructs a {@code ServerException} with the specified
     * detail message.
     *
     * @param returnCode the Response's returnCode.
     */
    public ServerException(String returnCode, String returnMsg) {
        super(returnMsg);
        this.returnCode = returnCode;
    }

    public String getReturnCode() {
        return returnCode;
    }
}

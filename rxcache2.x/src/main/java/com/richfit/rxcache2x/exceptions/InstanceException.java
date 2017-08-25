package com.richfit.rxcache2x.exceptions;


import java.lang.*;

/**
 * 实例化错误
 * @version monday 2016-07
 */
public class InstanceException extends Exception {

    public InstanceException() {
    }

    public InstanceException(String message) {
        super(message);
    }

    public InstanceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public InstanceException(Throwable throwable) {
        super(throwable);
    }

}

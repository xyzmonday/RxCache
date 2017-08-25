package com.richfit.rxcache2x.exceptions;

import java.lang.*;

/**
 * 没有实现接口的错误
 * @version monday 2016-07
 */
public class NotImplementException extends Exception {

    public NotImplementException() {
    }

    public NotImplementException(String message) {
        super(message);
    }

    public NotImplementException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public NotImplementException(Throwable throwable) {
        super(throwable);
    }

}

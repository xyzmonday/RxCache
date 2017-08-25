package com.richfit.rxcache2x.exceptions;

import java.lang.*;

/**
 * 参数错误
 * @version monday 2016-03
 */
public class ArgumentException extends Exception {

    public ArgumentException() {
    }

    public ArgumentException(String message) {
        super(message);
    }

    public ArgumentException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ArgumentException(Throwable throwable) {
        super(throwable);
    }

}

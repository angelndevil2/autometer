package com.tistory.devilnangel.autometer.common;

/**
 * @author k, Created on 16. 2. 2.
 */
public class AutoMeterException extends Exception {
    private static final long serialVersionUID = SerialNo.AUTOMETER_EXCEPTION;

    public AutoMeterException() {
    }

    public AutoMeterException(String message) {
        super(message);
    }

    public AutoMeterException(String message, Throwable cause) {
        super(message, cause);
    }

    public AutoMeterException(Throwable cause) {
        super(cause);
    }

    protected AutoMeterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

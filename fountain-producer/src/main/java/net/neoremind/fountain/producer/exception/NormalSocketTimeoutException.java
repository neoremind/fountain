package net.neoremind.fountain.producer.exception;

import java.io.IOException;

/**
 * socket读取异常
 */
public class NormalSocketTimeoutException extends IOException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NormalSocketTimeoutException(Throwable e) {
        super(e);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}

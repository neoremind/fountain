package net.neoremind.fountain.exception;

/**
 * GtId非法的运行时异常
 *
 * @author zhangxu
 */
public class GtIdInvalidException extends RuntimeException {

    public GtIdInvalidException() {
    }

    public GtIdInvalidException(String message) {
        super(message);
    }

    public GtIdInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public GtIdInvalidException(Throwable cause) {
        super(cause);
    }
}


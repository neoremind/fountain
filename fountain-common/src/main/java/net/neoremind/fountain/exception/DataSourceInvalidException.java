package net.neoremind.fountain.exception;

/**
 * @author zhangxu
 */
public class DataSourceInvalidException extends RuntimeException {

    private static final long serialVersionUID = -6354824241862111440L;

    public DataSourceInvalidException() {
        super();
    }

    public DataSourceInvalidException(String message) {
        super(message);
    }

    public DataSourceInvalidException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

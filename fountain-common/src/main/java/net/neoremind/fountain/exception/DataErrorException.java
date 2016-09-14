package net.neoremind.fountain.exception;

/**
 * @author zhangxu
 */
public class DataErrorException extends RuntimeException {
    private static final long serialVersionUID = 4467092659706397673L;

    public DataErrorException() {
        super();
    }

    public DataErrorException(String message) {
        super(message);
    }

    public DataErrorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

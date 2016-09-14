package net.neoremind.fountain.producer.exception;

/**
 * @author zhangxu
 */
public class ReplicationEventPositionInvalidException extends RuntimeException {

    private static final long serialVersionUID = 2447053697498090837L;

    public ReplicationEventPositionInvalidException() {
        super();
    }

    public ReplicationEventPositionInvalidException(String message) {
        super(message);
    }

    public ReplicationEventPositionInvalidException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

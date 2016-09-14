package net.neoremind.fountain.exception;

public class ParamErrorException extends RuntimeException {
    private static final long serialVersionUID = -219817271534193116L;

    public ParamErrorException() {
        super();
    }

    public ParamErrorException(String message) {
        super(message);
    }

    public ParamErrorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

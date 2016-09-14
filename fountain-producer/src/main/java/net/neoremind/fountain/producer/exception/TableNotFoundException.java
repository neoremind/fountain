package net.neoremind.fountain.producer.exception;

public class TableNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 7331139304350692523L;

    public TableNotFoundException() {
        super();
    }

    public TableNotFoundException(String message) {
        super(message);
    }

    public TableNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

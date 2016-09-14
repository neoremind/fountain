package net.neoremind.fountain.producer.dispatch;

public class DispatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DispatchException() {
        super();
    }

    public DispatchException(String msg) {
        super(msg);
    }
}

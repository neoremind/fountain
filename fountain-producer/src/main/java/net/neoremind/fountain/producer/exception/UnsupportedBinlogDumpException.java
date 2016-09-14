package net.neoremind.fountain.producer.exception;

/**
 * binglog dump策略发生和MySQL server不兼容的异常
 *
 * @author zhangxu
 */
public class UnsupportedBinlogDumpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsupportedBinlogDumpException() {
        super();
    }

    public UnsupportedBinlogDumpException(String message) {
        super(message);
    }

    public UnsupportedBinlogDumpException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public UnsupportedBinlogDumpException(Throwable throwable) {
        super(throwable);
    }

}

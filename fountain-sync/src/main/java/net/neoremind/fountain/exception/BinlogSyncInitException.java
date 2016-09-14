package net.neoremind.fountain.exception;

/**
 * binlog sync组件初始化异常
 *
 * @author zhangxu
 */
public class BinlogSyncInitException extends RuntimeException {

    private static final long serialVersionUID = 4467092659706397673L;

    public BinlogSyncInitException() {
        super();
    }

    public BinlogSyncInitException(String message) {
        super(message);
    }

    public BinlogSyncInitException(String message, Throwable throwable) {
        super(message, throwable);
    }

}

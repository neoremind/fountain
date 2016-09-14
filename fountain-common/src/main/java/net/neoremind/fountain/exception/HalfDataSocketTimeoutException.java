package net.neoremind.fountain.exception;

import java.net.SocketTimeoutException;

/**
 * 当接收event的一部部分数据后socket出现异常时抛出该异常
 *
 * @author hexiufeng
 */
public class HalfDataSocketTimeoutException extends SocketTimeoutException {

    private static final long serialVersionUID = 1L;
}

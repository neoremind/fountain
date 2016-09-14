package net.neoremind.fountain.producer;

/**
 * 控制连续的socket read data timeout，在某些特殊情况下会出现这种问题
 *
 * @author zhangxu
 */
public interface ConsequentSocketTimeoutHandler {
    /**
     * 清除以前记录的信息
     */
    void clean();

    /**
     * 处理本次timeout
     *
     * @return 是否需要外部处理
     */
    boolean handleTimeout();
}

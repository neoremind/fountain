package net.neoremind.fountain.producer.dispatch;

/**
 * 传输数据前的过滤器,在传输数据前给用户一个最后决定是否继续传输的机会
 *
 * @author hexiufeng
 */
public interface TransportFilter {
    /**
     * 是否通过过滤
     *
     * @param transObj
     *
     * @return
     */
    boolean filter(Object transObj);
}

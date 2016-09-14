package net.neoremind.fountain.producer.dispatch;

/**
 * 过滤掉为null的对象
 */
public class NullTransportFilter implements TransportFilter {

    @Override
    public boolean filter(Object transObj) {
        return transObj != null;
    }

}

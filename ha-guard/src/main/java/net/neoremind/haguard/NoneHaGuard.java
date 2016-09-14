package net.neoremind.haguard;

/**
 * 非高可用实现，所有fountain实例都竞争争抢dump，可能会因为slaveId重复而有人不能正常接同步二进制流。
 * <p/>
 * 如果slaveId恰巧不一致，多个实例中的线程可以同步，这本身没有问题，但是下游往往不希望重复消费，例如MQ推消息场景，一般不希望重复推送。
 *
 * @author hexiufeng, zhangxu
 */
public class NoneHaGuard extends AbstractHaGuard implements HaGuard {

    @Override
    public void init(String name) {

    }

    @Override
    public boolean takeToken(long timeout) {
        return true;
    }

    @Override
    public boolean hasToken() {
        return true;
    }
}

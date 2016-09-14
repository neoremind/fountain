package net.neoremind.haguard;

/**
 * 在HA环境中，控制哪个实例是当前leader的控制器，只有一个实例可以监控数据变化，其他的standby热备即可。
 *
 * @author hexiufeng, zhangxu
 */
public interface HaGuard {

    /**
     * 初始化
     */
    void init(String name);

    /**
     * 待超时的堵塞获取令牌
     *
     * @param timeout 超时时间，单位毫秒
     *
     * @return 是否获取令牌
     */
    boolean takeToken(long timeout);

    /**
     * 使用缺省超时的堵塞获取令牌
     *
     * @return 是否获取令牌
     */
    boolean takeTokenWithDefaultTimeout();

    /**
     * 是否已经获得了令牌
     *
     * @return boolean 是否获取令牌
     */
    boolean hasToken();

}

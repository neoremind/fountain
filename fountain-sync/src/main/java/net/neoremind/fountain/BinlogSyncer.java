package net.neoremind.fountain;

/**
 * Binlog同步器
 *
 * @author zhangxu
 */
public interface BinlogSyncer {

    /**
     * 开启同步
     */
    void start();

    /**
     * 停止并销毁
     */
    void stop();

}

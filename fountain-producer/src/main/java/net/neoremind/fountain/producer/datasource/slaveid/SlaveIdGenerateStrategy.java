package net.neoremind.fountain.producer.datasource.slaveid;

/**
 * MySQL的slaveId生成器
 *
 * @author zhangxu
 */
public interface SlaveIdGenerateStrategy<T extends Number> {

    /**
     * 获取slaveId
     *
     * @return slaveId
     */
    T get();

}

package net.neoremind.fountain.producer.datasource.binlogdump;

import net.neoremind.fountain.datasource.MysqlDataSource;

/**
 * binlog dump策略的getter接口，一般用于{@link MysqlDataSource}需要感知binlog dump策略的存在
 *
 * @author zhangxu
 */
public interface BinlogDumpStrategyAware {

    /**
     * 返回binlog dump策略
     *
     * @return binlog dump策略
     */
    BinlogDumpStrategy getBinlogDumpStrategy();

    /**
     * 设置binlog dump策略
     */
    void setBinlogDumpStrategy(BinlogDumpStrategy binlogDumpStrategy);

}

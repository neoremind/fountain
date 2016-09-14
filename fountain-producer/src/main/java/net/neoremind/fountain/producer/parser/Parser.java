package net.neoremind.fountain.producer.parser;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.event.EventHeader;
import net.neoremind.fountain.meta.TableMetaProvider;

/**
 * 解析binlog或者databus二进制数据的抽象接口
 *
 * @author zhangxu
 */
public interface Parser {
    /**
     * 解析协议数据的header，目前只适用于databus
     *
     * @param buf ByteBuffer
     *
     * @return EventHeader
     */
    <T extends EventHeader> T parseHeader(ByteBuffer buf);

    /**
     * 解析协议的每一个事件，目前只是用databus
     *
     * @param buf    ByteBuffer
     * @param header header
     *
     * @return BaseLogEvent
     */
    <T> BaseLogEvent parseDataToEvent(ByteBuffer buf, T header);

    /**
     * 指定表元数据的提供者，只适用于row base binlog。 row base binlog中不包含表的字段名称，需要从其他渠道获取
     *
     * @param tableMetaProvider TableMetaProvider
     */
    void setTableMetaProvider(TableMetaProvider tableMetaProvider);
}

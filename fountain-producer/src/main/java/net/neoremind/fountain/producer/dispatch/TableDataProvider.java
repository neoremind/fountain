package net.neoremind.fountain.producer.dispatch;

import java.math.BigInteger;
import java.util.List;

import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.meta.ColumnMeta;

/**
 * 表数据的提供者，策略模式，描述当前协议event解析后对外提供的基础数据
 *
 * @author hexiufeng
 */
public interface TableDataProvider {
    /**
     * 表名称
     *
     * @return
     */
    String getTableName();

    /**
     * 协议中本event所占的数据长度
     *
     * @return 数据长度
     */
    int getDataLen();

    /**
     * 列的元数据
     *
     * @return 列元数据列表
     */
    List<ColumnMeta> getColumnMeta();

    /**
     * 解析后的表数据
     *
     * @return 行数据列表
     */
    List<RowData> getRowData();

    /**
     * event的GT id
     *
     * @return gt id
     */
    BigInteger getGTId();
}

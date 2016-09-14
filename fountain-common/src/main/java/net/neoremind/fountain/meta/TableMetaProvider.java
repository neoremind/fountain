package net.neoremind.fountain.meta;

/**
 * row base binlog中不提供表中的列名称，只提供列索引，但解析binlog数据后下发时需要知道 列名，这些信息需要从其他来源获取，本接口就是表中列信息提供者的抽象描述
 *
 * @author hexiufeng
 */
public interface TableMetaProvider {
    /**
     * 根据给定的表的名称获取表的列信息元数据，由于表结构不经常变，所以表的元数据信息会被 缓存，但有时表结构也可能被修改，修改后需要重新获取元数据，有关表结构是否已经被修改 的信息由调用方来决定。
     * <p>
     * 一般来讲，当发现row base binlog中描述的表的字段数不同或类型不同，即表示表结构被 修改过了，此时需要重新获取表元数据
     * </p>
     *
     * @param tableFullName tableFullName
     * @param tableId       tableId
     *
     * @return TableMeta
     */
    TableMeta getTableMeta(String tableFullName, long tableId);
}

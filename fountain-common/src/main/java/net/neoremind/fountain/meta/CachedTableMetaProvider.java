package net.neoremind.fountain.meta;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.datasource.MysqlDataSource;

/**
 * 实现{@link TableMetaProvider TableMetaProvider}接口，支持对表元数据 进行缓存。 内部使用desc
 * tablename进行元数据的获取，使用Map进行缓存。
 *
 * @author hexiufeng
 */
public class CachedTableMetaProvider implements TableMetaProvider {
    private static final Logger logger = LoggerFactory.getLogger(CachedTableMetaProvider.class);
    /**
     * 用于缓存元数据的map 被单线程使用，没有线程安全问题
     */
    private Map<String, TableMeta> tableNameMetaMap = new HashMap<String, TableMeta>(128);
    /**
     * 用于执行desc table sql的数据源
     */
    private final MysqlDataSource mysqlDataSource;

    /**
     * 构造器
     *
     * @param mysqlDataSource MysqlDataSource
     */
    public CachedTableMetaProvider(MysqlDataSource mysqlDataSource) {
        this.mysqlDataSource = mysqlDataSource;
    }

    @Override
    public TableMeta getTableMeta(String tableFullName, long tableId) {
        TableMeta tableMeta = tableNameMetaMap.get(tableFullName);
        if (tableMeta != null) {
            if (tableMeta.getTableId() == tableId) {
                return tableMeta;
            } else {
                tableNameMetaMap.remove(tableFullName);
            }
        }
        try {
            tableMeta = mysqlDataSource.queryTableMeta(tableFullName);
            if (tableMeta != null) {
                tableMeta.setTableId(tableId);
                tableNameMetaMap.put(tableFullName, tableMeta);
                return tableMeta;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}

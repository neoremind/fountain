package net.neoremind.fountain.datasource;

import java.util.List;

/**
 * 在ha场景中,选择备选datasource的策略
 *
 * @author hexiufeng
 */
public interface DatasourceChoosePolicy {
    /**
     * 从给定的datasourceList选择合适的datasource,选择过程中如果需要读取数据或者初始化选中的datasource,
     * 调用callback来完成该任务
     *
     * @param datasourceList 备选的datasource
     * @param callback       用于读取数据或者初始化datasource的回调
     *
     * @return
     */
    <T extends MysqlDataSource> T choose(final List<T> datasourceList, DatasourceChooseCallbackHandler<T> callback);
}

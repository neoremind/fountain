package net.neoremind.fountain.datasource;

import java.util.List;

import net.neoremind.fountain.exception.DataSourceInvalidException;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

/**
 * Round Robin方式的选择datasource的策略
 *
 * @author hexiufeng
 */
@UnThreadSafe
public class RoundRobinDatasourceChoosePolicy implements DatasourceChoosePolicy {
    // 当前选择的datasource的index
    private int index;
    // 选择新的datasource之前是否需要wait time
    private long tryInterval = 0L;

    @Override
    public <T extends MysqlDataSource> T choose(List<T> datasourceList,
                                                DatasourceChooseCallbackHandler<T> callbackHandler) {
        if (tryInterval > 0) {
            // 选择新的数据源
            try {
                Thread.sleep(tryInterval);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        for (int i = 0; i < datasourceList.size(); i++) {
            T nextDs = getNextDatasouce(datasourceList);
            try {
                callbackHandler.doCallback(nextDs);
                return nextDs;
            } catch (Exception e) {
                callbackHandler.logError(e);
            }
        }
        throw new DataSourceInvalidException("All dataSources are not valid");
    }

    private <T> T getNextDatasouce(final List<T> datasourceList) {
        // 选择新的数据源
        if (index >= datasourceList.size()) {
            index = 0;
        }
        int pos = (index++) % datasourceList.size();
        return datasourceList.get(pos);
    }

    public long getTryInterval() {
        return tryInterval;
    }

    public void setTryInterval(long tryInterval) {
        this.tryInterval = tryInterval;
    }

}

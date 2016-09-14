package net.neoremind.fountain.datasource;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.exception.DataSourceInvalidException;

/**
 * 普通的非mysql binlog数据源的ha实现，该类暂时不会使用.
 *
 * @author hexiufeng, zhangxu
 */
public class HANormalMysqlDataSource extends AbstractHADatasource<MysqlDataSource> implements MysqlDataSource {
    private static final Logger logger = LoggerFactory.getLogger(HANormalMysqlDataSource.class);

    @Override
    protected <T> T doHaTask(final String command, String method, TaskExcutor<T> taskExecutor)
            throws NoSuchAlgorithmException, IOException {
        checkDataSourceListEmpty();

        for (int i = 0; i < mysqlDataSourceList.size(); i++) {
            try {
                return taskExecutor.execute(command, currentDataSource);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                chooseMysqlDataSource();
            }
        }

        throw new DataSourceInvalidException("all dataSources are not valid for " + method);
    }

    @Override
    protected void prepareChoosedDatasouce(MysqlDataSource choosedDattasource) throws IOException,
            NoSuchAlgorithmException, TimeoutException {
        choosedDattasource.open();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}

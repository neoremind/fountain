package net.neoremind.fountain.datasource;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * ha场景下选择数据源后初始化数据源或者读取数据的回调
 * @author hexiufeng
 *
 * @param <T>
 */
public interface DatasourceChooseCallbackHandler<T> {
    /**
     * 对数据源进行初始化或者其他操作
     * @param choosedDattasource
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws TimeoutException
     */
    void doCallback(T choosedDattasource) throws IOException, NoSuchAlgorithmException,TimeoutException;
    /**
     * 记录异常信息
     * @param e
     */
    void logError(Exception e);
}

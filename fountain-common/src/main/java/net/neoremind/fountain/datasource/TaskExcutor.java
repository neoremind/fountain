package net.neoremind.fountain.datasource;

/**
 * 任务执行器，用于{@link AbstractHADatasource#doHaTask doHaTask}
 * 方法,一般使用匿名类，任务的具体执行逻辑延迟到<b>doHaTask</b>方法调用时。
 *
 * @param <T>
 *
 * @author hexiufeng
 */
public interface TaskExcutor<T> {
    /**
     * 在指定的数据源上执行一个命令,命令是可枚举的,在调用
     * {@link AbstractHADatasource#doHaTask doHaTask}方法时确定其业务逻辑
     *
     * @param command
     * @param dataSouce
     *
     * @return
     *
     * @throws Exception
     */
    T execute(String command, MysqlDataSource dataSouce) throws Exception;
}

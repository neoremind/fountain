package net.neoremind.fountain.test.support;

/**
 * 任务接口
 *
 * @author zhangxu
 */
public interface Task {

    /**
     * 执行一些操作
     *
     * @throws InterruptedException
     */
    void execute() throws InterruptedException;

}

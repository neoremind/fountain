package net.neoremind.fountain.test.support;

/**
 * 异步线程
 *
 * @author zhangxu
 */
public class AsyncThread {

    /**
     * 根据Task接口异步跑线程
     *
     * @param task
     */
    public static void run(final Task task) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    task.execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}

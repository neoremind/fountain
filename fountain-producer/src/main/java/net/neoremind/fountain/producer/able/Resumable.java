package net.neoremind.fountain.producer.able;

/**
 * 可恢复的
 *
 * @author zhangxu
 */
public interface Resumable {

    /**
     * 恢复
     */
    void resume();

}

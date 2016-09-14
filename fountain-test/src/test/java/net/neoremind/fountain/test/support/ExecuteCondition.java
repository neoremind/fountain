package net.neoremind.fountain.test.support;

/**
 * 验证条件
 *
 * @author zhangxu
 */
public interface ExecuteCondition {

    /**
     * 是否准好可以开始
     *
     * @return
     */
    boolean isReady();

    /**
     * 是否可以退出
     *
     * @return
     */
    boolean isStop();

    /**
     * 默认从这个数值开始检查changedataset
     * <p/>
     * 对于MySQL5.6来说，传递gtidset，最后一个interval end也会传过来，因此验证时候需要去掉
     *
     * @return
     */
    int neglectChangeDataSetNum();

}

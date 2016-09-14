package net.neoremind.fountain.producer.datasource.slaveid;

/**
 * 每次都重置
 *
 * @author zhangxu
 */
public class ResetEveryTime implements Resettable {

    @Override
    public boolean isEnableReset() {
        return true;
    }

}

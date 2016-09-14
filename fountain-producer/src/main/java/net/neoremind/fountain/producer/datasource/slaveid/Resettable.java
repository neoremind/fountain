package net.neoremind.fountain.producer.datasource.slaveid;

/**
 * @author zhangxu
 */
public interface Resettable<T extends Number> {

    boolean isEnableReset();

}

package net.neoremind.fountain;

/**
 * 构造器接口
 *
 * @author zhangxu
 */
interface Builder<T> {

    /**
     * 构造对象
     *
     * @return 构造成功的对象
     */
    T build();

}

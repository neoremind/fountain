package net.neoremind.fountain;

/**
 * 2个对象的wrapper接口
 *
 * @author zhangxu
 */
public interface P2<A, B> {

    /**
     * 返回第一个对象
     *
     * @return 第一个对象
     */
    A _1();

    /**
     * 返回第二个对象
     *
     * @return 第二个对象
     */
    B _2();
}

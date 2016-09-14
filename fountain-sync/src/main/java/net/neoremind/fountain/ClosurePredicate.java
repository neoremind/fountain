package net.neoremind.fountain;

/**
 * 闭包方式的条件验证，一般调用方将待验证的值放入这个接口的实现，后续直接调用{@linkplain #apply()}来做判断是true还是false
 *
 * @author zhangxu
 */
interface ClosurePredicate<T> {

    /**
     * 将传入的值进行验证，返回true或者false
     *
     * @return 返回值
     */
    boolean apply();

}

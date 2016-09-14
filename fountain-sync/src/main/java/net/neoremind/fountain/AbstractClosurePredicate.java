package net.neoremind.fountain;

/**
 * 抽象的闭包条件验证
 *
 * @author zhangxu
 */
abstract class AbstractClosurePredicate<T> implements ClosurePredicate<T>, Settable<ClosurePredicate, T> {

    /**
     * 待验证对象
     */
    protected T t;

    /**
     * 默认构造方法
     */
    public AbstractClosurePredicate() {
    }

    /**
     * 构造方法
     *
     * @param t 待验证对象
     */
    public AbstractClosurePredicate(T t) {
        this.t = t;
    }

    @Override
    public ClosurePredicate set(T t) {
        this.t = t;
        return this;
    }

    /**
     * 一个模板方法，先判断可验，再验证
     *
     * @return 满足条件返回true，否则false
     */
    @Override
    public boolean apply() {
        if (canApply()) {
            return doApply();
        }
        return false;
    }

    /**
     * 是否可以进行验证
     *
     * @return 可以验证返回true，否则false
     */
    public abstract boolean canApply();

    /**
     * 进行验证
     *
     * @return 满足条件返回true，否则false
     */
    public abstract boolean doApply();
}

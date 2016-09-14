package net.neoremind.fountain;

/**
 * 对象不为空的条件验证
 *
 * @author zhangxu
 */
class NotNullClosurePredicate<T> extends AbstractClosurePredicate<T> implements ClosurePredicate<T> {

    public NotNullClosurePredicate(T t) {
        super(t);
    }

    /**
     * 静态构造方法
     *
     * @param t   待验证对象
     * @param <T> 对象的类型
     *
     * @return 对象不为空的条件验证
     */
    public static <T> NotNullClosurePredicate of(T t) {
        return new NotNullClosurePredicate(t);
    }

    @Override
    public boolean doApply() {
        return t != null;
    }

    @Override
    public boolean canApply() {
        return true;
    }
}

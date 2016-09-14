package net.neoremind.fountain;

/**
 * 一个“非A即B”的抽象工具类。
 * <p/>
 * 用于提供默认值，当满足一定条件的是否才返回指定的对象的场景，可以做到链式调用，可读性性很强
 *
 * @author zhangxu
 */
public class Either<T> {

    /**
     * 默认值
     */
    private T defaultValue;

    /**
     * 条件验证，满足是否不返回默认值，返回指定值
     */
    private ClosurePredicate<?> predicate;

    /**
     * 禁止直接构造
     */
    private Either() {

    }

    /**
     * 传入默认值的构造方法
     *
     * @param defaultValue 默认值
     */
    public Either(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * 传入默认值的静态构造方法
     *
     * @param defaultValue 默认值
     * @param <T>          默认值类型
     *
     * @return Either本身
     */
    public static <T> Either<T> or(T defaultValue) {
        return new Either<T>(defaultValue);
    }

    /**
     * 从一个可为空的对象来构造，当为空时放回默认值{@link #defaultValue}，否则返回传入的值{@code t}
     *
     * @param t 传入对象
     *
     * @return 返回对象
     */
    public T fromNullable(T t) {
        if (t == null) {
            return defaultValue;
        } else {
            return t;
        }
    }

    /**
     * 如果满足条件验证，惰性求值，仅先保存条件
     *
     * @param predicate 条件验证
     *
     * @return Either本身
     */
    public Either<T> ifApply(ClosurePredicate<?> predicate) {
        this.predicate = predicate;
        return this;
    }

    /**
     * 配合{@link #ifApply(ClosurePredicate)}使用，如果条件验证不为空并且返回true，则返回{@code t}，否则返回默认值{@link #defaultValue}
     *
     * @param t 传入对象
     *
     * @return 返回对象
     */
    public T thenReturn(T t) {
        if (predicate != null) {
            if (predicate.apply()) {
                return t;
            }
        }
        return defaultValue;
    }

}

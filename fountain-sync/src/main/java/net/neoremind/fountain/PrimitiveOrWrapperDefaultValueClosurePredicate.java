package net.neoremind.fountain;

/**
 * 基本类型或者装箱类型是否为默认值的条件验证
 *
 * @author zhangxu
 */
class PrimitiveOrWrapperDefaultValueClosurePredicate<T> extends AbstractClosurePredicate<T>
        implements ClosurePredicate<T> {

    public PrimitiveOrWrapperDefaultValueClosurePredicate() {
    }

    public PrimitiveOrWrapperDefaultValueClosurePredicate(T t) {
        super(t);
    }

    /**
     * 静态构造方法
     *
     * @param t   待验证对象
     * @param <T> 对象的类型
     *
     * @return 基本类型或者装箱类型是否为默认值的条件验证
     */
    public static <T> PrimitiveOrWrapperDefaultValueClosurePredicate of(T t) {
        return new PrimitiveOrWrapperDefaultValueClosurePredicate(t);
    }

    @Override
    public boolean doApply() {
        if (t.getClass().isPrimitive()) {
            return t != ClassUtil.getPrimitiveDefaultValue(t.getClass());
        }
        if (ClassUtil.isPrimitiveWrapper(t.getClass())) {
            return t != ClassUtil.getPrimitiveDefaultValue(ClassUtil.getPrimitiveType(t.getClass()));
        }
        return false;
    }

    @Override
    public boolean canApply() {
        return t.getClass().isPrimitive() || ClassUtil.isPrimitiveWrapper(t.getClass());
    }
}

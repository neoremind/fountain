package net.neoremind.fountain;

import org.apache.commons.lang3.StringUtils;

/**
 * 字符串是否为空的条件验证
 *
 * @author zhangxu
 */
class StringNotNullClosurePredicate extends AbstractClosurePredicate<String> implements ClosurePredicate<String> {

    public StringNotNullClosurePredicate() {
    }

    public StringNotNullClosurePredicate(String t) {
        super(t);
    }

    /**
     * 静态构造方法
     *
     * @param t   待验证对象
     * @param <T> 对象的类型
     *
     * @return 字符串是否为空的条件验证
     */
    public static StringNotNullClosurePredicate of(String t) {
        return new StringNotNullClosurePredicate(t);
    }

    @Override
    public boolean doApply() {
        return StringUtils.isNotEmpty(t);
    }

    @Override
    public boolean canApply() {
        return t instanceof String;
    }
}

package net.neoremind.fountain;

import java.util.Arrays;
import java.util.List;

/**
 * 条件验证的逻辑工厂
 *
 * @author zhangxu
 */
class Predicates {

    /**
     * 返回多个条件验证，用或来做逻辑
     *
     * @param components 多个条件验证
     * @param <T>        待验证对象类型
     *
     * @return 或逻辑的条件验证
     */
    public static <T> OrPredicate<T> or(ClosurePredicate<T>... components) {
        return new OrPredicate<T>(Arrays.asList(components));
    }

    /**
     * 返回多个条件验证，用与来做逻辑
     *
     * @param components 多个条件验证
     * @param <T>        待验证对象类型
     *
     * @return 与逻辑的条件验证
     */
    public static <T> AndPredicate<T> and(ClosurePredicate<T>... components) {
        return new AndPredicate<T>(Arrays.asList(components));
    }

    /**
     * 或逻辑的条件验证类
     *
     * @param <T> 待验证对象类型
     */
    private static class OrPredicate<T> implements ClosurePredicate<T> {
        private final List<? extends ClosurePredicate<? super T>> components;

        private OrPredicate(List<? extends ClosurePredicate<? super T>> components) {
            this.components = components;
        }

        @Override
        public boolean apply() {
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i).apply()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 与逻辑的条件验证类
     *
     * @param <T> 待验证对象类型
     */
    private static class AndPredicate<T> implements ClosurePredicate<T> {
        private final List<? extends ClosurePredicate<? super T>> components;

        private AndPredicate(List<? extends ClosurePredicate<? super T>> components) {
            this.components = components;
        }

        @Override
        public boolean apply() {
            for (int i = 0; i < components.size(); i++) {
                if (!components.get(i).apply()) {
                    return false;
                }
            }
            return false;
        }
    }
}

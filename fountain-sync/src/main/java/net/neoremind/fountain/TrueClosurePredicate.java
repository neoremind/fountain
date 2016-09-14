package net.neoremind.fountain;

/**
 * 永远为真的条件验证
 *
 * @author zhangxu
 */
class TrueClosurePredicate<T> implements ClosurePredicate<T> {

    @Override
    public boolean apply() {
        return true;
    }

}

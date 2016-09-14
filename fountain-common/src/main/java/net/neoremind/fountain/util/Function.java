package net.neoremind.fountain.util;

/**
 * Determines an output value based on an input value.
 *
 * @author zhangxu
 */
public interface Function<F, T> {

    /**
     * Returns the result of applying this function to {@code input}.
     */
    T apply(F input);

}

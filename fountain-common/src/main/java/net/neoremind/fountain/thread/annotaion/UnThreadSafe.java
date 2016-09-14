package net.neoremind.fountain.thread.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于说明类或者方法时非线程安全的,只是帮助调用者识别调用的对象是否支持线程安全
 *
 * @author hexiufeng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface UnThreadSafe {

}

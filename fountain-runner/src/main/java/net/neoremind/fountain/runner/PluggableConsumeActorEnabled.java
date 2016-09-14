package net.neoremind.fountain.runner;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.neoremind.fountain.consumer.spi.ConsumeActor;
import net.neoremind.fountain.consumer.spi.Consumer;

/**
 * fountain-runner内部使用，为{@link Consumer}定制的特殊注解，仅仅是一个标识。
 * <p/>
 * 可插拔的设计，可以在运行时注入自定义的消费者{@link ConsumeActor}
 *
 * @author zhangxu
 * @see ConsumeActor
 * @see Consumer
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface PluggableConsumeActorEnabled {

}

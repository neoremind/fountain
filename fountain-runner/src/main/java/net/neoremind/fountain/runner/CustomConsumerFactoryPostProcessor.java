package net.neoremind.fountain.runner;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

import net.neoremind.fountain.consumer.spi.ConsumeActor;
import net.neoremind.fountain.consumer.spi.ConsumeActorAware;
import net.neoremind.fountain.consumer.spi.Consumer;

/**
 * 在Spring初始化过程中对于bean的后置处理。
 * <p/>
 * 这里是为了满足{@link Consumer}中的
 * {@link ConsumeActor}中的可插拔设计，而实现的。
 * <p/>
 * 通过运行时动态的注解{@link PluggableConsumeActorEnabled}扫描Consumer，
 * 去指定的classpath下load一个Spring的XML配置，新建一个IoC容器，作为主容器的孩子。然后将这个新
 * 容器中的{@link ConsumeActor}覆盖到主容器中Consumer的属性引用，就可以做到一个可插拔的特性。
 * <p/>
 * 例如，fountain-runner.jar和bigpipe-consumer.jar，{@link ConsumeActor}在bigpipe-consumer.jar中，
 * 在同一个classpath下，那么fountain-runner.jar就可以运行时动态load进入容器并且覆盖引用。
 *
 * @author zhangxu
 */
public class CustomConsumerFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomConsumerFactoryPostProcessor.class);

    /**
     * 默认构造方法
     *
     * @param resourceXmlPath    自定义消费者在classpath下的路径，例如<tt>appContext-fountain-consumer-demo.xml</tt>
     * @param applicationContext fountain的spring主上下文容器
     */
    public CustomConsumerFactoryPostProcessor(String resourceXmlPath,
                                              ConfigurableApplicationContext applicationContext) {
        this.resourceXmlPath = resourceXmlPath;
        this.applicationContext = applicationContext;
    }

    /**
     * classpath前缀，不使用<code>classpath*:</code>，默认只加载第一个找到的配置文件
     */
    public static final String CLASSPATH_PREFIX = "classpath:";

    /**
     * 自定义消费者在classpath下的路径，例如<tt>appContext-fountain-consumer-demo.xml</tt>
     */
    private String resourceXmlPath;

    /**
     * fountain的spring主上下文容器。<br/>
     * 自定义消费者的上下文<code>applicationContext</code>会挂在在这个下面，成为一个子容器
     */
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.addBeanPostProcessor(new CustomConsumerPostProcessor());
    }

    /**
     * 处理fountain的spring主上下文容器中的指定的某个消费者，将自定义的消费者注入到
     */
    public class CustomConsumerPostProcessor implements BeanPostProcessor {

        private ClassPathXmlApplicationContext iocContainer;

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            PluggableConsumeActorEnabled meta =
                    AnnotationUtils.findAnnotation(bean.getClass(), PluggableConsumeActorEnabled.class);

            if (meta != null) {
                if (!(bean instanceof ConsumeActorAware)) {
                    LOGGER.error(
                            "PluggableConsumeActorEnabled annotaion can only be used on ConsumeActorAware "
                                    + "implementation");
                }

                LOGGER.info("Start to init IoC container by loading XML bean definitions from {}",
                        CLASSPATH_PREFIX + resourceXmlPath);
                iocContainer = new ClassPathXmlApplicationContext(CLASSPATH_PREFIX + resourceXmlPath);
                iocContainer.setParent(applicationContext);
                Map<String, ConsumeActor> consumeActorBeanMap = iocContainer.getBeansOfType(ConsumeActor.class);

                if (CollectionUtils.isEmpty(consumeActorBeanMap)) {
                    throw new BeanInitializationException(
                            "No ConsumeActor bean found in " + CLASSPATH_PREFIX + resourceXmlPath);
                }

                if (consumeActorBeanMap.size() > 1) {
                    LOGGER.warn("Multiple ConsumeActor beans found in {}{}, and the first one found will be used"
                            + " only", CLASSPATH_PREFIX, resourceXmlPath);
                } else {
                    LOGGER.info("Find one ConsumeActor which will be plugged into Consumer as an actor");
                }

                ConsumeActor consumeActor = consumeActorBeanMap.values().toArray(new ConsumeActor[] {})[0];
                ((ConsumeActorAware) bean).setConsumeActor(consumeActor);
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }
    }
}

package net.neoremind.fountain.runner;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 自定义的的可插拔bean后置处理注入到IoC容器初始化上下文中
 *
 * @author zhangxu
 */
public class FountainConsumerApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * consume actor的XML文件的path，去掉classpath*:或者classpath:前缀
     */
    private String consumeActorXmlPath;

    /**
     * 构造方法
     *
     * @param consumeActorXmlPath
     */
    public FountainConsumerApplicationContextInitializer(String consumeActorXmlPath) {
        this.consumeActorXmlPath = consumeActorXmlPath;
    }

    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        applicationContext.addBeanFactoryPostProcessor(
                new CustomConsumerFactoryPostProcessor(consumeActorXmlPath, applicationContext));
    }

    public String getConsumeActorXmlPath() {
        return consumeActorXmlPath;
    }

    public void setConsumeActorXmlPath(String consumeActorXmlPath) {
        this.consumeActorXmlPath = consumeActorXmlPath;
    }
}

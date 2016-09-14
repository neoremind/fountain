package net.neoremind.fountain.runner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import net.neoremind.fountain.consumer.spi.ConsumeActor;

/**
 * Fountain主进程入口
 * <p/>
 * 运行步骤如下：<br/>
 * 1）加载<tt>classpath:applicationContext-fountain-runner.xml</tt>来初始化Spring环境，作为父IoC容器。<br/>
 * 2）根据Spring的profile来进行具体方案的选择，这里的方案是指是数据源类型，比如MySQL 5.1或者5.6等，通过参数
 * -Dspring.profiles.active=mysql.rowbase51来指定。<br/>
 * 3）加载指定数据源的XML配置，会初始化fountain的bean，这些bean就在JAVA进程中存活，生命周期完全托管Spring管理，
 * 外界可通过kill -15或者kill -9 通知信号量给JVM的PID，来进行shutdown。<br/>
 * 4）第三步中需要的一切环境变量，默认都在各自profile目录下的properties中存在，生产环境覆盖，利用Spring boot，
 * 通过linux shell环境变量或者命令行参数按需覆盖。优先级见注1。
 * 5）对于一些Consumer可支持定制化，通过{@link ConsumeActor}来做可插拔特性，
 * 在第三步profile的XML初始化Spring IoC容器时候，会动态根据参数load外部的XML，做子IoC容器，将需要的ConsumeActor
 * bean自动注入到主IoC容器中的Consumer中。
 * <p/>
 * 启动命令示例如下：
 * java -classpath ${CUR_CLASSPATH}
 * -Dspring.profiles.active=mysql.rowbase51
 * -Dmysql_shard_0_server="172.20.133.73"
 * -Dmysql_shard_0_ha1_server="172.20.133.73"
 * </pre>
 * 这里注意，当环境变量中有mysql_shard_0_server时，可以省略掉后两个配置等。
 * <p/>
 * 注1：Spring Boot的PropertySource优先级顺序如下:
 * <p/>
 * <ul>
 * <li>1）Command line arguments.</li>
 * <li>2）Properties from SPRING_APPLICATION_JSON (inline JSON embedded in an environment variable or system
 * property)</li>
 * <li>3）JNDI attributes from java:comp/env.</li>
 * <li>4）Java System properties (System.getProperties()).</li>
 * <li>5）OS environment variables.</li>
 * <li>6）A RandomValuePropertySource that only has properties in random.*.</li>
 * <li>7）Profile-specific application properties outside of your packaged jar (application-{profile}.properties and
 * YAMLvariants)</li>
 * <li>8）Profile-specific application properties packaged inside your jar (application-{profile}.properties and YAML
 * variants)</li>
 * <li>9）Application properties outside of your packaged jar (application.properties and YAML variants).</li>
 * <li>10）Application properties packaged inside your jar (application.properties and YAML variants).</li>
 * </ul>
 * 参考如下链接：
 * <pre>
 * https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
 * https://docs.spring.io/spring-boot/docs/current/reference/html/howto-properties-and-configuration.html
 * https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-configuration-classes.html
 * https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html
 * </pre>
 *
 * @author zhangxu
 */
@SpringBootApplication
@ImportResource("classpath:applicationContext-fountain-runner.xml")
public class FountainMain {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(FountainMain.class);
        springApplication.setBanner(new FountainBanner());
        String consumeActorXmlPath = SysPropUtil.getSystemProperty("consume.actor.xml.classpath");
        if (StringUtils.isNotEmpty(consumeActorXmlPath)) {
            springApplication.addInitializers(new FountainConsumerApplicationContextInitializer(consumeActorXmlPath));
        }
        springApplication.run(args);
    }

}

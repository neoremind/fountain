package net.neoremind.fountain.examples.inprocess;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.producer.dispatch.transcontrol.NonTransactionPolicy;

/**
 * 百度Ares MySQL5.1版本示例
 * <p/>
 * <ul>
 * <li>1、使用百度Gtid作为持久化eventposition</li>
 * <li>2、使用{@link NonTransactionPolicy}作为event的事务处理策略</li>
 * </ul>
 *
 * 带有JMX监控的，可改变fountain内部的一些运行状态，例如suspend、resume同步线程，打印profiling信息以及获取最近的同步点信息等。
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:baidu.ares.mysql.rowbase51-nontrx-jmx/fountain-config.xml"})
public class BaiduAresRowbase51NontrxJmx {

    @Test
    public void test() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

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
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:baidu.ares.mysql.rowbase51-nontrx/fountain-config.xml"})
public class BaiduAresRowbase51Nontrx {

    @Test
    public void test() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

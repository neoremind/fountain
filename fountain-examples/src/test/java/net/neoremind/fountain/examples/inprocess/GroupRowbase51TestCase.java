package net.neoremind.fountain.examples.inprocess;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 百度MySQL Ares 5.1版本为例展示Group简化多数据源接入的配置
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:rowbase51-group/fountain-config.xml"})
public class GroupRowbase51TestCase {

    @Test
    public void test() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

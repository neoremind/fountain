package net.neoremind.fountain.examples.inprocess;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 打包发送到rabbit例子
 *
 * @author hexiufeng
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:rabbit/fountain-producer.xml", "classpath:rabbit/rabbitmq.xml"})
public class RabbitPacketRowbaseTestCase {

    @Test
    public void test() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

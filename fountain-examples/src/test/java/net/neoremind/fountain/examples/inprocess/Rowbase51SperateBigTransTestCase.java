package net.neoremind.fountain.examples.inprocess;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * baidu mysql 5.1 rowbase binlog 切割大事务示例。
 * <p/>
 * <ul>
 * <li> 整事务接收数据，但下发时切割事务 </li>
 * <li> 内存缓冲队列的最大容量m只要大于切割后的数据条数即可</li>
 * <li> 支持innodb和myisam混用</li>
 * <li> 可能消耗更多内存</li>
 * </ul>
 *
 * @author hexiufeng
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:separatebigtrans/fountain-config.xml"})
public class Rowbase51SperateBigTransTestCase {

    @Test
    public void test() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

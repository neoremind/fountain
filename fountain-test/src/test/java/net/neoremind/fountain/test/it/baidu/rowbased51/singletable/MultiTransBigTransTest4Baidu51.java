package net.neoremind.fountain.test.it.baidu.rowbased51.singletable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.test.it.template.singletable.MultiTransBigTransTest;

/**
 * 5个事务，每个事务都是insert多条数据，但是其中第二条事务是插入1000条insert
 * <p/>
 * 用于模拟大事务情况，使用{@link net.neoremind.fountain.producer.dispatch.transcontrol
 * .MiniTransactionPolicy}会积攒所有的event，merge在一起再下发。
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:baidu/rowbased51/fountain-config.xml"})
public class MultiTransBigTransTest4Baidu51 extends MultiTransBigTransTest {

    @Test
    public void testMultiTransBigTrans() {
        super.testMultiTransBigTrans();
    }

}
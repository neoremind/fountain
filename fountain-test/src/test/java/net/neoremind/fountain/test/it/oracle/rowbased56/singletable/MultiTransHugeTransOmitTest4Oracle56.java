package net.neoremind.fountain.test.it.oracle.rowbased56.singletable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.test.it.template.singletable.MultiTransHugeTransOmitTest;

/**
 * 5个事务，每个事务都是insert多条数据，但是其中第二条事务是插入1100条insert
 * <p/>
 * 用于模拟大事务情况，使用{@link net.neoremind.fountain.producer.dispatch.transcontrol
 * .MiniTransactionPolicy}会积攒所有的event，merge在一起再下发，该testcase中一次性插入的数据超过了maxtTransLen，
 * 因此这条变化被Omit掉了，预期收到5个事件，实际得到4个。
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:oracle/rowbased56/fountain-config.xml"})
public class MultiTransHugeTransOmitTest4Oracle56 extends MultiTransHugeTransOmitTest {

    @Test
    public void testMultiTransHugeTransOmit() {
        super.testMultiTransHugeTransOmit();
    }

    @Override
    protected int getNeglectChangeDataSetNum() {
        return 1;
    }
}
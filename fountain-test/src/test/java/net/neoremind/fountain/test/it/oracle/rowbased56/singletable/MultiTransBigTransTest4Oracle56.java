package net.neoremind.fountain.test.it.oracle.rowbased56.singletable;

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
 * <p/>
 * 注意：运行该case由于有{@link #getNeglectChangeDataSetNum()}的存在，因此验证assert的时候
 * 会跳过一条错误，如果最开始的一个testcase是{@link MultiTransHugeTransOmitTest4Oracle56}
 * 那么delete所有会触发一个大事务，fountain使用的minitrx策略会omit掉，因此会出现
 * <pre>
 *     java.lang.AssertionError: Should get 6 ChangeDataSet, but get 5
 * </pre>
 * 所以不要再这个case后面跑这个。
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:oracle/rowbased56/fountain-config.xml"})
public class MultiTransBigTransTest4Oracle56 extends MultiTransBigTransTest {

    @Test
    public void testMultiTransBigTrans() {
        super.testMultiTransBigTrans();
    }

    @Override
    protected int getNeglectChangeDataSetNum() {
        return 1;
    }
}
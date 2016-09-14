package net.neoremind.fountain.test.it.oracle.rowbased56.singletable.nontrx;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.event.RowEvent;
import net.neoremind.fountain.test.it.template.singletable.nontrx.MultiTransMultiInsertRowPerTransTest;
import net.neoremind.fountain.producer.dispatch.transcontrol.NonTransactionPolicy;

/**
 * 5个事务，每个事务都是insert多条数据。
 * <p/>
 * 使用了{@link NonTransactionPolicy}，
 * 因此下发的{@link RowEvent}不会积攒，而是来多少下发多少，这个testcase中
 * 如果是百度自己的mysql则会一条整数据下来，100条封装在1个event里。
 * <p/>
 * 如果是官方的则会，
 * 第一次会insert100条数据，MySQL master server会拆分为<tt>16+15+...+15+9</tt>共7次event过来。
 * <p/>
 * 因此assert会出现
 * <pre>
 *     java.lang.AssertionError: Should get 12 ChangeDataSet, but get 6
 * </pre>
 *
 * @author zhangxu
 */
//FIXME 官方的ok，百度的mysql assert失败，但是数据没问题，就是条数打包问题
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:oracle/rowbased56/fountain-config-nontrx.xml"})
public class MultiTransMultiInsertRowPerTransTest4Oracle56 extends MultiTransMultiInsertRowPerTransTest {

    @Test
    public void testMultiTransMultiInsertRowPerTrans() {
        super.testMultiTransMultiInsertRowPerTrans();
    }

    @Override
    protected int getNeglectChangeDataSetNum() {
        return 1;
    }
}
package net.neoremind.fountain.test.it.baidu.rowbased51.singletable.nontrx;

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
 * 第一次会insert100条数据，MySQL master server会拆分为<tt>16+15+...+15+9</tt>共7次event过来。
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:baidu/rowbased51/fountain-config-nontrx.xml"})
public class MultiTransMultiInsertRowPerTransTest4Baidu51 extends MultiTransMultiInsertRowPerTransTest {

    @Test
    public void testMultiTransMultiInsertRowPerTrans() {
        super.testMultiTransMultiInsertRowPerTrans();
    }

}
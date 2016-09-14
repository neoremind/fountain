package net.neoremind.fountain.test.it.oracle.rowbased51.singletable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.test.it.template.singletable.MultiTransMultiInsertRowPerTransTest;

/**
 * 5个事务，每个事务都是insert多条数据
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:oracle/rowbased51/fountain-config.xml"})
public class MultiTransMultiInsertRowPerTransTest4Oracle51 extends MultiTransMultiInsertRowPerTransTest {

    @Test
    public void testMultiTransMultiInsertRowPerTrans() {
        super.testMultiTransMultiInsertRowPerTrans();
    }

}
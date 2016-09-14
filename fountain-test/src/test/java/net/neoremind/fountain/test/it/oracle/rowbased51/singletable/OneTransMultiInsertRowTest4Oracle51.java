package net.neoremind.fountain.test.it.oracle.rowbased51.singletable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.test.it.template.singletable.OneTransMultiInsertRowTest;

/**
 * 一个事务多条insert数据
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:oracle/rowbased51/fountain-config.xml"})
public class OneTransMultiInsertRowTest4Oracle51 extends OneTransMultiInsertRowTest {

    @Test
    public void testOneTransMultiInsertRow() {
        super.testOneTransMultiInsertRow();
    }

}
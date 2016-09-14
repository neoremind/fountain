package net.neoremind.fountain.test.it.oracle.rowbased56.singletable;

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
@ContextConfiguration(locations = {"classpath:oracle/rowbased56/fountain-config.xml"})
public class OneTransMultiInsertRowTest4Oracle56 extends OneTransMultiInsertRowTest {

    @Test
    public void testOneTransMultiInsertRow() {
        super.testOneTransMultiInsertRow();
    }

    @Override
    protected int getNeglectChangeDataSetNum() {
        return 1;
    }

}
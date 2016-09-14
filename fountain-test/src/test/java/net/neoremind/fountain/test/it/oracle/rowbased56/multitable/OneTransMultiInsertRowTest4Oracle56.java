package net.neoremind.fountain.test.it.oracle.rowbased56.multitable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.test.it.template.multitable.OneTransMultiInsertRowTest;

/**
 * 一个事务多条insert数据，分了两张表，student表先插入5条，之后city表再插入3条
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
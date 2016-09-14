package net.neoremind.fountain.test.it.oracle.rowbased51.singletable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.test.it.template.singletable.OneTransHybridInsertUpdateDeleteRowTest;

/**
 * 一个事务，这个事务里混合了insert、update、delete操作，混合操作是按照事务中提交的顺序来的
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:oracle/rowbased51/fountain-config.xml"})
public class OneTransHybridInsertUpdateDeleteRowTest4Oracle51 extends OneTransHybridInsertUpdateDeleteRowTest {

    @Test
    public void testOneTransHybridInsertUpdateDeleteRow() {
        super.testOneTransHybridInsertUpdateDeleteRow();
    }

}
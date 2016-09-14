package net.neoremind.fountain.test.it.oracle.rowbased51.singletable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.test.it.template.singletable.MultiTransInsertUpdateDeleteRowEachTransTest;

/**
 * 3个事务，第一个事务是update一条数据，第二个是delete三条数据，第三个是insert五条数据
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:oracle/rowbased51/fountain-config.xml"})
public class MultiTransInsertUpdateDeleteRowEachTransTest4Oracle51
        extends MultiTransInsertUpdateDeleteRowEachTransTest {

    @Test
    public void testMultiTransInsertUpdateDeleteRowEachTrans() {
        super.testMultiTransInsertUpdateDeleteRowEachTrans();
    }

}
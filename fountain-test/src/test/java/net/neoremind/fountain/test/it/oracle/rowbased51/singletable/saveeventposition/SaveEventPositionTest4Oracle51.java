package net.neoremind.fountain.test.it.oracle.rowbased51.singletable.saveeventposition;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.neoremind.fountain.test.it.template.singletable.saveeventposition.SaveEventPositionTest;

/**
 * 2个事务，每个事务都是insert 1条数据
 * <p/>
 * 然后验证同步点文件是否存储了正确的gtid
 *
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:oracle/rowbased51/fountain-config.xml"})
public class SaveEventPositionTest4Oracle51 extends SaveEventPositionTest {

    @Test
    public void testSaveEventPosition() {
        super.testSaveEventPosition4BinlogFilenamePosition();
    }

}
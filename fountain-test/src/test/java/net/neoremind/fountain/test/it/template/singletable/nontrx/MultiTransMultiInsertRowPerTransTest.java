package net.neoremind.fountain.test.it.template.singletable.nontrx;

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.test.constants.TableName;
import net.neoremind.fountain.test.it.template.BaseTest;
import net.neoremind.fountain.test.support.AsyncChangeHandler;
import com.google.common.collect.Maps;

import net.neoremind.fountain.event.RowEvent;
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
public class MultiTransMultiInsertRowPerTransTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(MultiTransMultiInsertRowPerTransTest.class);

    private Map<Integer, Integer> eventSeq2AffectedRowNumber = Maps.newHashMap();

    {
        eventSeq2AffectedRowNumber.put(0, 100);
        eventSeq2AffectedRowNumber.put(1, 9);
        eventSeq2AffectedRowNumber.put(2, 8);
        eventSeq2AffectedRowNumber.put(3, 7);
        eventSeq2AffectedRowNumber.put(4, 6);
    }

    @Test
    public void testMultiTransMultiInsertRowPerTrans() {
        AsyncChangeHandler asyncChangeHandler = new AsyncChangeHandler() {
            @Override
            public void doExecuteSql() {
                studentService.createStudents(100);
                studentService.createStudents(9);
                studentService.createStudents(8);
                studentService.createStudents(7);
                studentService.createStudents(6);
            }

            /**
             * TODO 可能为12，因此此case有可能失败
             *
             * @return
             */
            @Override
            public int getTransactionNumber() {
                return 11;
            }

            @Override
            public void doCheckEachEvent(ChangeDataSet changeDataSet, int eventSeq) {
                printTableDef(changeDataSet, 1);
                validateTableDef4Student(changeDataSet.getTableDef().get(TableName.STUDENT_TABLE_NAME));

                printTableData(changeDataSet, 1);
                logger.info(changeDataSet.getDataSize() + "");
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

}
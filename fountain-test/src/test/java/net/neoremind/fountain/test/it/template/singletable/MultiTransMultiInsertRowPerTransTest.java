package net.neoremind.fountain.test.it.template.singletable;

import java.util.Map;

import org.junit.Test;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.test.constants.TableName;
import net.neoremind.fountain.test.it.template.BaseTest;
import net.neoremind.fountain.test.support.AsyncChangeHandler;
import com.google.common.collect.Maps;

/**
 * 5个事务，每个事务都是insert多条数据
 *
 * @author zhangxu
 */
public class MultiTransMultiInsertRowPerTransTest extends BaseTest {

    private Map<Integer, Integer> eventSeq2AffectedRowNumber = Maps.newHashMap();

    {
        eventSeq2AffectedRowNumber.put(0, 10);
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
                studentService.createStudents(10);
                studentService.createStudents(9);
                studentService.createStudents(8);
                studentService.createStudents(7);
                studentService.createStudents(6);
            }

            @Override
            public int getTransactionNumber() {
                return 5;
            }

            @Override
            public void doCheckEachEvent(ChangeDataSet changeDataSet, int eventSeq) {
                printTableDef(changeDataSet, 1);
                validateTableDef4Student(changeDataSet.getTableDef().get(TableName.STUDENT_TABLE_NAME));

                printTableData(changeDataSet, 1);
                checkStudentRowData4Insert(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME),
                        eventSeq2AffectedRowNumber.get(eventSeq - getNeglectChangeDataSetNum()), 3, 3);
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

}
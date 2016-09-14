package net.neoremind.fountain.test.it.template.singletable;

import org.junit.Test;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.test.constants.TableName;
import net.neoremind.fountain.test.it.template.BaseTest;
import net.neoremind.fountain.test.support.AsyncChangeHandler;

/**
 * 一个事务，一条insert数据
 *
 * @author zhangxu
 */
public class OneTransOneInsertRowTest extends BaseTest {

    @Test
    public void testOneTransOneInsertRow() {
        AsyncChangeHandler asyncChangeHandler = new AsyncChangeHandler() {
            @Override
            public void doExecuteSql() {
                studentService.createStudents(1);
            }

            @Override
            public int getTransactionNumber() {
                return 1;
            }

            @Override
            public void doCheckEachEvent(ChangeDataSet changeDataSet, int eventSeq) {
                printTableDef(changeDataSet, 1);
                validateTableDef4Student(changeDataSet.getTableDef().get(TableName.STUDENT_TABLE_NAME));

                printTableData(changeDataSet, 1);
                checkStudentRowData4Insert(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 1, 0, 0);
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

}
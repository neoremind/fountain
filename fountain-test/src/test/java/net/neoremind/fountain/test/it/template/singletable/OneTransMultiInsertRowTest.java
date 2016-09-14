package net.neoremind.fountain.test.it.template.singletable;

import org.junit.Test;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.test.constants.TableName;
import net.neoremind.fountain.test.it.template.BaseTest;
import net.neoremind.fountain.test.support.AsyncChangeHandler;

/**
 * 一个事务多条insert数据
 *
 * @author zhangxu
 */
public class OneTransMultiInsertRowTest extends BaseTest {

    @Test
    public void testOneTransMultiInsertRow() {
        AsyncChangeHandler asyncChangeHandler = new AsyncChangeHandler() {
            @Override
            public void doExecuteSql() {
                studentService.createStudents(10);
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
                checkStudentRowData4Insert(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 10, 5, 5);
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

}
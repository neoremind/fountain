package net.neoremind.fountain.test.it.template.singletable;

import org.junit.Before;
import org.junit.Test;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.test.constants.TableName;
import net.neoremind.fountain.test.it.template.BaseTest;
import net.neoremind.fountain.test.support.AsyncChangeHandler;

/**
 * 一个事务，这个事务里混合了insert、update、delete操作，混合操作是按照事务中提交的顺序来的
 *
 * @author zhangxu
 */
public class OneTransHybridInsertUpdateDeleteRowTest extends BaseTest {

    /**
     * 删除表并新增默认10条
     */
    @Before
    public void prepare() {
        studentService.deleteAll();
        studentService.createStudents(10);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOneTransHybridInsertUpdateDeleteRow() {
        AsyncChangeHandler asyncChangeHandler = new AsyncChangeHandler() {
            @Override
            public void doExecuteSql() {
                myFacade.doHybridStudents();
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
                checkStudentRowData4Update(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 9, 0, 5);
                checkStudentRowData4Delete(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 9, 2, 8);
                checkStudentRowData4Insert(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 9, 5, 1);
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

}
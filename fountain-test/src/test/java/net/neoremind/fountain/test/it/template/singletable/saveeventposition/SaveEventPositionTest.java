package net.neoremind.fountain.test.it.template.singletable.saveeventposition;

import org.junit.Test;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.test.constants.TableName;
import net.neoremind.fountain.test.it.template.BaseTest;
import net.neoremind.fountain.test.support.AsyncChangeHandler;

/**
 * 2个事务，每个事务都是insert 1条数据
 * <p/>
 * 然后验证同步点文件是否存储了正确的gtid或者binlog filename、position
 *
 * @author zhangxu
 */
public class SaveEventPositionTest extends BaseTest {

    @Test
    public void testSaveEventPosition4GtId() {
        AsyncChangeHandler asyncChangeHandler = new AsyncChangeHandler() {
            @Override
            public void doExecuteSql() {
                studentService.createStudents(1);
                studentService.createStudents(1);
            }

            @Override
            public int getTransactionNumber() {
                return 2;
            }

            @Override
            public void doCheckEachEvent(ChangeDataSet changeDataSet, int eventSeq) {
                printTableDef(changeDataSet, 1);
                validateTableDef4Student(changeDataSet.getTableDef().get(TableName.STUDENT_TABLE_NAME));

                printTableData(changeDataSet, 1);
                checkStudentRowData4Insert(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 1, 0, 0);

                // 由于使用的了delay groupid的event position，第一次调用save不会记录
                // 因此这里只验证第二次的点。
                savePoint(changeDataSet);

                if (eventSeq > 0) {
                    validatePoint(changeDataSet, (changeDataSet.getGtId().longValue() - 1L) + "", null);
                }
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

    @Test
    public void testSaveEventPosition4BinlogFilenamePosition() {
        AsyncChangeHandler asyncChangeHandler = new AsyncChangeHandler() {
            @Override
            public void doExecuteSql() {
                studentService.createStudents(1);
                studentService.createStudents(1);
            }

            @Override
            public int getTransactionNumber() {
                return 2;
            }

            @Override
            public void doCheckEachEvent(ChangeDataSet changeDataSet, int eventSeq) {
                printTableDef(changeDataSet, 1);
                validateTableDef4Student(changeDataSet.getTableDef().get(TableName.STUDENT_TABLE_NAME));

                printTableData(changeDataSet, 1);
                checkStudentRowData4Insert(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 1, 0, 0);

                savePoint(changeDataSet);

                validatePoint(changeDataSet, "mysql-bin.[0-9]+", "binlog");
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

    @Test
    public void testSaveEventPosition4GtIdSet() {
        AsyncChangeHandler asyncChangeHandler = new AsyncChangeHandler() {
            @Override
            public void doExecuteSql() {
                studentService.createStudents(1);
                studentService.createStudents(1);
            }

            @Override
            public int getTransactionNumber() {
                return 2;
            }

            @Override
            public void doCheckEachEvent(ChangeDataSet changeDataSet, int eventSeq) {
                printTableDef(changeDataSet, 1);
                validateTableDef4Student(changeDataSet.getTableDef().get(TableName.STUDENT_TABLE_NAME));

                printTableData(changeDataSet, 1);
                checkStudentRowData4Insert(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 1, 0, 0);

                savePoint(changeDataSet);

                validatePoint(changeDataSet, "[0-9]*-[0-9]*", "gtidset");
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

}
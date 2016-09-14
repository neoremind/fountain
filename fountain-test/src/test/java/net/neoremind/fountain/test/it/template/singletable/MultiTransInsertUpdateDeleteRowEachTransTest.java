package net.neoremind.fountain.test.it.template.singletable;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.test.constants.TableName;
import net.neoremind.fountain.test.it.template.BaseTest;
import net.neoremind.fountain.test.po.Student;
import net.neoremind.fountain.test.support.AsyncChangeHandler;
import com.google.common.collect.Lists;

/**
 * 3个事务，第一个事务是update一条数据，第二个是delete三条数据，第三个是insert五条数据
 *
 * 准备工作时先delete所有，然后再插入10个student
 *
 * @author zhangxu
 */
public class MultiTransInsertUpdateDeleteRowEachTransTest extends BaseTest {

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
    public void testMultiTransInsertUpdateDeleteRowEachTrans() {
        AsyncChangeHandler asyncChangeHandler = new AsyncChangeHandler() {
            @Override
            public void doExecuteSql() {
                List<Student> allStudents = studentService.getAll();
                Student toUpdate = allStudents.get(5);

                // 更新第5个，从0开始
                studentService.updateStudent(toUpdate.getStudentId());

                // 删除第6、8、9共3个，从0开始
                List<Long> toDeleteIds = Lists.newArrayList(
                        allStudents.get(6).getStudentId(),
                        allStudents.get(8).getStudentId(),
                        allStudents.get(9).getStudentId());
                studentService.deleteStudentByIds(toDeleteIds);

                // 新建5个
                studentService.createStudents(5);
            }

            @Override
            public int getTransactionNumber() {
                return 3;
            }

            @Override
            public void doCheckEachEvent(ChangeDataSet changeDataSet, int eventSeq) {
                printTableDef(changeDataSet, 1);
                validateTableDef4Student(changeDataSet.getTableDef().get(TableName.STUDENT_TABLE_NAME));

                printTableData(changeDataSet, 1);
                if (eventSeq - getNeglectChangeDataSetNum() == 0) {
                    checkStudentRowData4Update(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 1, 0, 5);
                } else if (eventSeq - getNeglectChangeDataSetNum() == 1) {
                    checkStudentRowData4Delete(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 3, 1, 8);
                } else if (eventSeq - getNeglectChangeDataSetNum() == 2) {
                    checkStudentRowData4Insert(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 5, 3, 3);
                } else {
                    fail("Eventseq wrong");
                }
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

}
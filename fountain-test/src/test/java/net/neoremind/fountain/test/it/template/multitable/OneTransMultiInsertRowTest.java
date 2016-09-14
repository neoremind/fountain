package net.neoremind.fountain.test.it.template.multitable;

import org.junit.Test;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.test.constants.TableName;
import net.neoremind.fountain.test.it.template.BaseTest;
import net.neoremind.fountain.test.support.AsyncChangeHandler;

/**
 * 一个事务多条insert数据，分了两张表，student表先插入5条，之后city表再插入3条
 *
 * @author zhangxu
 */
public class OneTransMultiInsertRowTest extends BaseTest {

    @Test
    public void testOneTransMultiInsertRow() {
        AsyncChangeHandler asyncChangeHandler = new AsyncChangeHandler() {
            @Override
            public void doExecuteSql() {
                myFacade.doMultiTable();
            }

            @Override
            public int getTransactionNumber() {
                return 1;
            }

            @Override
            public void doCheckEachEvent(ChangeDataSet changeDataSet, int eventSeq) {
                printTableDef(changeDataSet, 2);
                validateTableDef4Student(changeDataSet.getTableDef().get(TableName.STUDENT_TABLE_NAME));
                validateTableDef4City(changeDataSet.getTableDef().get(TableName.CITY_TABLE_NAME));

                printTableData(changeDataSet, 2);
                checkStudentRowData4Insert(changeDataSet.getTableData().get(TableName.STUDENT_TABLE_NAME), 5, 3, 3);
                checkCityRowData4Insert(changeDataSet.getTableData().get(TableName.CITY_TABLE_NAME), 3, 1, 1);
            }
        };
        doIntegrationTest(asyncChangeHandler);
    }

}
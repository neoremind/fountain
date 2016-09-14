package net.neoremind.fountain.test.it.template;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.meta.ColumnTypeEnum;
import net.neoremind.fountain.meta.MetaDefine;
import net.neoremind.fountain.producer.DefaultProducer;
import net.neoremind.fountain.test.facade.MyFacade;
import net.neoremind.fountain.test.service.CityService;
import net.neoremind.fountain.test.service.StudentService;
import net.neoremind.fountain.test.support.AsyncChangeHandler;
import net.neoremind.fountain.test.support.CityHelper;
import net.neoremind.fountain.test.support.DateUtil;
import net.neoremind.fountain.test.support.EventHolder;
import net.neoremind.fountain.test.support.ExecuteCondition;
import net.neoremind.fountain.test.support.StudentHelper;

/**
 * 验证的基类
 *
 * @author zhangxu
 */
public class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    protected static final int DEFAULT_COUNTDOWN_LATCH_TIME_IN_SECS = 20;

    /**
     * 桥接的同步点
     */
    @Resource
    private DisposeEventPositionBridge bridge;

    @Resource
    protected EventHolder eventHolder;

    @Resource
    protected DefaultProducer defaultProducer;

    @Resource
    protected StudentService studentService;

    @Resource
    protected CityService cityService;

    @Resource
    protected MyFacade myFacade;

    /**
     * 模拟真实生产环境验证
     * <p/>
     * <ul>
     * <li>1) 启动JUnit测试用例，初始化Spring，会根据fountain的xml启动接收binlog的producer和处理变化的{@link net.neoremind.fountain.test.consumer
     * .TestConsumer}</li>
     * <li>2) 通过{@link AsyncChangeHandler}启动检查线程，{@link AsyncChangeHandler#checkEvent(EventHolder,
     * CountDownLatch, ExecuteCondition)}来接收Consumer推送变化ChangeDataSet到{@link
     * EventHolder}这个阻塞队列，针对每个变化用模板模式抽象出一个方法验证每个变化{@link AsyncChangeHandler#doCheckEachEvent(ChangeDataSet, int)}</li>
     * <li>3) 第二步当中先启动异步线程，不会有任何变化放入EventHolder，直到通过{@link AsyncChangeHandler}启动插入线程{@link
     * AsyncChangeHandler#executeSql(ExecuteCondition)}，才会有事件过来，这样就可以验证了</li>
     * <li>4) 启动了#2和#3的两个异步线程后，就用闭锁等待他们处理，一般来说在{@link AsyncChangeHandler#checkEvent(EventHolder, CountDownLatch,
     * ExecuteCondition)}中没处理好一个变化事件，就是调用闭锁的countDown()方法，因此按照预期的变化数量{@link AsyncChangeHandler#getTransactionNumber()
     * }之后，闭锁会自动退出</li>
     * <li>5) 验证预期的变化数量事件是否和实际EventHolder中获得的数量相等，不相等肯定不预期</li>
     * </ul>
     *
     * @param asyncChangeHandler
     */
    protected void doIntegrationTest(AsyncChangeHandler asyncChangeHandler) {
        final CountDownLatch latch = new CountDownLatch(asyncChangeHandler.getTransactionNumber());

        ExecuteCondition executeCondition = new ExecuteCondition() {
            @Override
            public boolean isReady() {
                return defaultProducer.getDataSource().isOpen();
            }

            @Override
            public boolean isStop() {
                return latch.getCount() == 0;
            }

            @Override
            public int neglectChangeDataSetNum() {
                return getNeglectChangeDataSetNum();
            }
        };

        asyncChangeHandler.checkEvent(eventHolder, latch, executeCondition);
        asyncChangeHandler.executeSql(executeCondition);

        try {
            latch.await(DEFAULT_COUNTDOWN_LATCH_TIME_IN_SECS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int actualNumber = asyncChangeHandler.getTransactionNumber()
                + executeCondition.neglectChangeDataSetNum();
        if (eventHolder.getEventCount() != actualNumber) {
            fail("Should get " + actualNumber + " ChangeDataSet, but get "
                    + eventHolder.getEventCount());
        }
    }

    /**
     * 每次跑完testcase清空表
     */
    @After
    public void clear() {
        studentService.deleteAll();
        cityService.deleteAll();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证学生表的插入增量变化
     *
     * @param tableData          表变化list
     * @param totalRowNumber     预期变化的数量，就是tableData的size()
     * @param verfiedRowNum      对于变化过来的增量验证第几个row，从0开始。准备验证哪个tableData中的元素，因此新增的testcase都是按照{@link
     *                           StudentHelper#getMultipleStudents(int)}规则插入的所以可以这么验证，从0开始
     * @param insertedStudentSeq 待验证的row对应原来表里被更新的第几个学生
     */
    protected void checkStudentRowData4Insert(List<RowData> tableData, int totalRowNumber, int verfiedRowNum,
                                              int insertedStudentSeq) {
        assertThat(tableData != null, is(true));
        logger.info("Expected row number is " + totalRowNumber + ", actual row number is " + tableData.size());
        assertThat(tableData.size(), is(totalRowNumber));
        assertThat(tableData.get(verfiedRowNum).isWrite(), is(true));
        assertThat(tableData.get(verfiedRowNum).isUpdate(), is(false));
        assertThat(tableData.get(verfiedRowNum).isDelete(), is(false));
        List<ColumnData> beforeColumnList = tableData.get(verfiedRowNum).getBeforeColumnList();
        assertThat(beforeColumnList.size(), is(0));
        List<ColumnData> afterColumnList = tableData.get(verfiedRowNum).getAfterColumnList();
        assertThat(afterColumnList.size(), is(12));

        assertThat((Long) (afterColumnList.get(0).getValue()), greaterThanOrEqualTo(0L));
        assertThat((Long) (afterColumnList.get(1).getValue()), greaterThanOrEqualTo(
                StudentHelper.STUDENT_ID_PREFIX + insertedStudentSeq));
        assertThat((Integer) (afterColumnList.get(2).getValue()),
                is(StudentHelper.SCHOOL_ID_PREFIX + insertedStudentSeq));
        assertThat((Integer) (afterColumnList.get(3).getValue()), is(insertedStudentSeq));
        assertThat((String) (afterColumnList.get(4).getValue()),
                is(StudentHelper.STUDENT_NAME_PREFIX + insertedStudentSeq));
        assertThat((Short) (afterColumnList.get(5).getValue()), is((short) (insertedStudentSeq % Integer.MAX_VALUE)));
        assertThat((Byte) (afterColumnList.get(6).getValue()), is((byte) (insertedStudentSeq % Byte.MAX_VALUE)));
        assertThat((String) (afterColumnList.get(7).getValue()),
                is(StudentHelper.DESCRIPTION_PREFIX + insertedStudentSeq));
        assertThat(DateUtil.isSameDay(
                (Date) (afterColumnList.get(8).getValue()),
                DateUtil.addDays(StudentHelper.BIRTH_DAY_START, insertedStudentSeq)
        ), is(true));
        assertThat(DateUtil.isSameDay(
                (Date) (afterColumnList.get(9).getValue()),
                DateUtil.addDays(StudentHelper.ONBOARD_TIME_START, insertedStudentSeq)
        ), is(true));
        assertThat(DateUtil.isSameDay(
                (Date) (afterColumnList.get(10).getValue()),
                DateUtil.getCurrentDate()
        ), is(true));
        assertThat((Float) (afterColumnList.get(11).getValue()), is(3.14f));
    }

    /**
     * 验证学生表的更新增量变化
     *
     * @param tableData         表变化list
     * @param totalRowNumber    预期变化的数量，就是tableData的size()
     * @param verfiedRowNum     对于变化过来的增量验证第几个row，从0开始
     * @param updatedStudentSeq 待验证的row对应原来表里被更新的第几个学生
     */
    public void checkStudentRowData4Update(List<RowData> tableData, int totalRowNumber, int verfiedRowNum, int
            updatedStudentSeq) {
        assertThat(tableData != null, is(true));
        logger.info("Expected row number is " + totalRowNumber + ", actual row number is " + tableData.size());
        assertThat(tableData.size(), is(totalRowNumber));
        assertThat(tableData.get(verfiedRowNum).isWrite(), is(false));
        assertThat(tableData.get(verfiedRowNum).isUpdate(), is(true));
        assertThat(tableData.get(verfiedRowNum).isDelete(), is(false));
        List<ColumnData> beforeColumnList = tableData.get(verfiedRowNum).getBeforeColumnList();
        assertThat(beforeColumnList.size(), is(12));
        List<ColumnData> afterColumnList = tableData.get(verfiedRowNum).getAfterColumnList();
        assertThat(afterColumnList.size(), is(12));

        assertThat((Long) (beforeColumnList.get(0).getValue()), greaterThanOrEqualTo(0L));
        assertThat((Long) (beforeColumnList.get(1).getValue()), greaterThanOrEqualTo(
                StudentHelper.STUDENT_ID_PREFIX + updatedStudentSeq));
        assertThat((Integer) (beforeColumnList.get(2).getValue()),
                is(StudentHelper.SCHOOL_ID_PREFIX + updatedStudentSeq));
        assertThat((Integer) (beforeColumnList.get(3).getValue()), is(updatedStudentSeq));
        assertThat((String) (beforeColumnList.get(4).getValue()),
                is(StudentHelper.STUDENT_NAME_PREFIX + updatedStudentSeq));
        assertThat((Short) (beforeColumnList.get(5).getValue()), is((short) (updatedStudentSeq % Integer.MAX_VALUE)));
        assertThat((Byte) (beforeColumnList.get(6).getValue()), is((byte) (updatedStudentSeq % Byte.MAX_VALUE)));
        assertThat((String) (beforeColumnList.get(7).getValue()),
                is(StudentHelper.DESCRIPTION_PREFIX + updatedStudentSeq));
        assertThat(DateUtil.isSameDay(
                (Date) (beforeColumnList.get(8).getValue()),
                DateUtil.addDays(StudentHelper.BIRTH_DAY_START, updatedStudentSeq)
        ), is(true));
        assertThat(DateUtil.isSameDay(
                (Date) (beforeColumnList.get(9).getValue()),
                DateUtil.addDays(StudentHelper.ONBOARD_TIME_START, updatedStudentSeq)
        ), is(true));
        assertThat(DateUtil.isSameDay(
                (Date) (beforeColumnList.get(10).getValue()),
                DateUtil.getCurrentDate()
        ), is(true));
        assertThat((Float) (beforeColumnList.get(11).getValue()), is(3.14f));

        assertThat((Long) (afterColumnList.get(0).getValue()), greaterThanOrEqualTo(0L));
        assertThat((Long) (afterColumnList.get(1).getValue()), greaterThanOrEqualTo(
                StudentHelper.STUDENT_ID_PREFIX + verfiedRowNum));
        assertThat((Integer) (afterColumnList.get(2).getValue()), is(StudentHelper.getSingleStudent().getSchoolId()));
        assertThat((Integer) (afterColumnList.get(3).getValue()), is(StudentHelper.getSingleStudent().getClassId()));
        assertThat((String) (afterColumnList.get(4).getValue()),
                is(StudentHelper.getSingleStudent().getStudentName()));
        assertThat((Short) (afterColumnList.get(5).getValue()),
                is((short) (StudentHelper.getSingleStudent().getAge())));
        // DAO没有更新gender
        assertThat((Byte) (afterColumnList.get(6).getValue()),
                is((byte) (updatedStudentSeq % Byte.MAX_VALUE)));
        assertThat((String) (afterColumnList.get(7).getValue()),
                is(StudentHelper.getSingleStudent().getDescription()));
        // DAO没有更新birthday
        assertThat(DateUtil.isSameDay(
                (Date) (afterColumnList.get(8).getValue()),
                DateUtil.addDays(StudentHelper.BIRTH_DAY_START, updatedStudentSeq)
        ), is(true));
        assertThat(DateUtil.isSameDay(
                (Date) (afterColumnList.get(9).getValue()),
                StudentHelper.getSingleStudent().getOnboardTime()
        ), is(true));
        assertThat(DateUtil.isSameDay(
                (Date) (afterColumnList.get(10).getValue()),
                DateUtil.getCurrentDate()
        ), is(true));
        assertThat((Float) (afterColumnList.get(11).getValue()), is(3.14f));
    }

    /**
     * 验证学生表的更新增量变化
     *
     * @param tableData      表变化list
     * @param totalRowNumber 预期变化的数量，就是tableData的size()
     * @param verfiedRowNum  对于变化过来的增量验证第几个row，从0开始
     */
    protected void checkStudentRowData4Delete(List<RowData> tableData, int totalRowNumber, int verfiedRowNum,
                                              int deletedStudentSeq) {
        assertThat(tableData != null, is(true));
        logger.info("Expected row number is " + totalRowNumber + ", actual row number is " + tableData.size());
        assertThat(tableData.size(), is(totalRowNumber));
        assertThat(tableData.get(verfiedRowNum).isWrite(), is(false));
        assertThat(tableData.get(verfiedRowNum).isUpdate(), is(false));
        assertThat(tableData.get(verfiedRowNum).isDelete(), is(true));
        List<ColumnData> beforeColumnList = tableData.get(verfiedRowNum).getBeforeColumnList();
        assertThat(beforeColumnList.size(), is(12));
        List<ColumnData> afterColumnList = tableData.get(verfiedRowNum).getAfterColumnList();
        assertThat(afterColumnList.size(), is(0));

        assertThat((Long) (beforeColumnList.get(0).getValue()), greaterThanOrEqualTo(0L));
        assertThat((Long) (beforeColumnList.get(1).getValue()), greaterThanOrEqualTo(
                StudentHelper.STUDENT_ID_PREFIX + deletedStudentSeq));
        assertThat((Integer) (beforeColumnList.get(2).getValue()),
                is(StudentHelper.SCHOOL_ID_PREFIX + deletedStudentSeq));
        assertThat((Integer) (beforeColumnList.get(3).getValue()), is(deletedStudentSeq));
        assertThat((String) (beforeColumnList.get(4).getValue()),
                is(StudentHelper.STUDENT_NAME_PREFIX + deletedStudentSeq));
        assertThat((Short) (beforeColumnList.get(5).getValue()), is((short) (deletedStudentSeq % Integer.MAX_VALUE)));
        assertThat((Byte) (beforeColumnList.get(6).getValue()), is((byte) (deletedStudentSeq % Byte.MAX_VALUE)));
        assertThat((String) (beforeColumnList.get(7).getValue()),
                is(StudentHelper.DESCRIPTION_PREFIX + deletedStudentSeq));
        assertThat(DateUtil.isSameDay(
                (Date) (beforeColumnList.get(8).getValue()),
                DateUtil.addDays(StudentHelper.BIRTH_DAY_START, deletedStudentSeq)
        ), is(true));
        assertThat(DateUtil.isSameDay(
                (Date) (beforeColumnList.get(9).getValue()),
                DateUtil.addDays(StudentHelper.ONBOARD_TIME_START, deletedStudentSeq)
        ), is(true));
        assertThat(DateUtil.isSameDay(
                (Date) (beforeColumnList.get(10).getValue()),
                DateUtil.getCurrentDate()
        ), is(true));
        assertThat((Float) (beforeColumnList.get(11).getValue()), is(3.14f));
    }

    /**
     * 验证城市表的插入增量变化
     *
     * @param tableData       表变化list
     * @param totalRowNumber  预期变化的数量，就是tableData的size()
     * @param verfiedRowNum   对于变化过来的增量验证第几个row，从0开始。准备验证哪个tableData中的元素，因此新增的testcase都是按照{@link
     *                        StudentHelper#getMultipleStudents(int)}规则插入的所以可以这么验证，从0开始
     * @param insertedCitySeq 待验证的row对应原来表里被更新的第几个城市
     */
    protected void checkCityRowData4Insert(List<RowData> tableData, int totalRowNumber, int verfiedRowNum,
                                           int insertedCitySeq) {
        assertThat(tableData != null, is(true));
        logger.info("Expected row number is " + totalRowNumber + ", actual row number is " + tableData.size());
        assertThat(tableData.size(), is(totalRowNumber));
        assertThat(tableData.get(verfiedRowNum).isWrite(), is(true));
        assertThat(tableData.get(verfiedRowNum).isUpdate(), is(false));
        assertThat(tableData.get(verfiedRowNum).isDelete(), is(false));
        List<ColumnData> beforeColumnList = tableData.get(verfiedRowNum).getBeforeColumnList();
        assertThat(beforeColumnList.size(), is(0));
        List<ColumnData> afterColumnList = tableData.get(verfiedRowNum).getAfterColumnList();
        assertThat(afterColumnList.size(), is(3));

        assertThat((Long) (afterColumnList.get(0).getValue()), greaterThanOrEqualTo(0L));
        assertThat((Integer) (afterColumnList.get(1).getValue()), greaterThanOrEqualTo(
                CityHelper.CITY_ID_PREFIX + insertedCitySeq));
        assertThat((String) (afterColumnList.get(2).getValue()), is(CityHelper.CITY_NAME_PREFIX + insertedCitySeq));
    }

    /**
     * 验证student表结构
     *
     * @param columnMetas
     */
    protected void validateTableDef4Student(List<ColumnMeta> columnMetas) {
        assertThat(columnMetas != null, is(true));
        assertThat(columnMetas.size(), is(12));
        // id
        assertThat(columnMetas.get(0).getColumnName(), is("id"));
        assertThat(columnMetas.get(0).getColumnType(), is("bigint(20)"));
        assertThat(columnMetas.get(0).getGeneralColumnType(), is("bigint"));
        assertThat(columnMetas.get(0).getKeyFlag(), is(MetaDefine.KEY_FLAG.PRIMARY_KEY));
        assertThat(columnMetas.get(0).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(0).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_LONGLONG));
        assertThat(columnMetas.get(0).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(0).getCharset(), nullValue());
        assertThat(columnMetas.get(0).getShortCharset(), nullValue());

        // student_id
        assertThat(columnMetas.get(1).getColumnName(), is("student_id"));
        assertThat(columnMetas.get(1).getColumnType(), is("bigint(20)"));
        assertThat(columnMetas.get(1).getGeneralColumnType(), is("bigint"));
        assertThat(columnMetas.get(1).getKeyFlag(), is(MetaDefine.KEY_FLAG.UNIQUE_KEY));
        assertThat(columnMetas.get(1).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(1).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_LONGLONG));
        assertThat(columnMetas.get(1).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(1).getCharset(), nullValue());
        assertThat(columnMetas.get(1).getShortCharset(), nullValue());

        // school_id
        assertThat(columnMetas.get(2).getColumnName(), is("school_id"));
        assertThat(columnMetas.get(2).getColumnType(), is("int(10)"));
        assertThat(columnMetas.get(2).getGeneralColumnType(), is("int"));
        assertThat(columnMetas.get(2).getKeyFlag(), is(MetaDefine.KEY_FLAG.MULTIPLE_KEY));
        assertThat(columnMetas.get(2).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(2).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_LONG));
        assertThat(columnMetas.get(2).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(2).getCharset(), nullValue());
        assertThat(columnMetas.get(2).getShortCharset(), nullValue());

        // class_id
        assertThat(columnMetas.get(3).getColumnName(), is("class_id"));
        assertThat(columnMetas.get(3).getColumnType(), is("mediumint(10)"));
        assertThat(columnMetas.get(3).getGeneralColumnType(), is("mediumint"));
        assertThat(columnMetas.get(3).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(3).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(3).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_INT24));
        assertThat(columnMetas.get(3).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(3).getCharset(), nullValue());
        assertThat(columnMetas.get(3).getShortCharset(), nullValue());

        // student_name
        assertThat(columnMetas.get(4).getColumnName(), is("student_name"));
        assertThat(columnMetas.get(4).getColumnType(), is("varchar(128)"));
        assertThat(columnMetas.get(4).getGeneralColumnType(), is("varchar"));
        assertThat(columnMetas.get(4).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(4).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(4).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_VARCHAR));
        assertThat(columnMetas.get(4).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(4).getCharset(), is("utf8_general_ci"));
        assertThat(columnMetas.get(4).getShortCharset(), is("utf8"));

        // age
        assertThat(columnMetas.get(5).getColumnName(), is("age"));
        assertThat(columnMetas.get(5).getColumnType(), is("smallint(3)"));
        assertThat(columnMetas.get(5).getGeneralColumnType(), is("smallint"));
        assertThat(columnMetas.get(5).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(5).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.YES));
        assertThat(columnMetas.get(5).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_SHORT));
        assertThat(columnMetas.get(5).getDefaultValue(), is("7"));
        assertThat(columnMetas.get(5).getCharset(), nullValue());
        assertThat(columnMetas.get(5).getShortCharset(), nullValue());

        // gender
        assertThat(columnMetas.get(6).getColumnName(), is("gender"));
        assertThat(columnMetas.get(6).getColumnType(), is("tinyint(1)"));
        assertThat(columnMetas.get(6).getGeneralColumnType(), is("tinyint"));
        assertThat(columnMetas.get(6).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(6).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(6).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_TINY));
        assertThat(columnMetas.get(6).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(6).getCharset(), nullValue());
        assertThat(columnMetas.get(6).getShortCharset(), nullValue());

        // description
        assertThat(columnMetas.get(7).getColumnName(), is("description"));
        assertThat(columnMetas.get(7).getColumnType(), is("text"));
        assertThat(columnMetas.get(7).getGeneralColumnType(), is("text"));
        assertThat(columnMetas.get(7).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(7).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(7).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_BLOB));
        assertThat(columnMetas.get(7).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(7).getCharset(), is("utf8_general_ci"));
        assertThat(columnMetas.get(7).getShortCharset(), is("utf8"));

        // birth_day
        assertThat(columnMetas.get(8).getColumnName(), is("birth_day"));
        assertThat(columnMetas.get(8).getColumnType(), is("date"));
        assertThat(columnMetas.get(8).getGeneralColumnType(), is("date"));
        assertThat(columnMetas.get(8).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(8).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(8).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_DATE));
        assertThat(columnMetas.get(8).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(8).getCharset(), nullValue());
        assertThat(columnMetas.get(8).getShortCharset(), nullValue());

        // onboard_time
        assertThat(columnMetas.get(9).getColumnName(), is("onboard_time"));
        assertThat(columnMetas.get(9).getColumnType(), is("datetime"));
        assertThat(columnMetas.get(9).getGeneralColumnType(), is("datetime"));
        assertThat(columnMetas.get(9).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(9).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat((columnMetas.get(9).getTypeEnum() == ColumnTypeEnum.MYSQL_TYPE_DATETIME
                            || columnMetas.get(9).getTypeEnum() == ColumnTypeEnum.MYSQL_TYPE_DATETIME2), is(
                is(true)));
        assertThat(columnMetas.get(9).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(9).getCharset(), nullValue());
        assertThat(columnMetas.get(9).getShortCharset(), nullValue());

        // update_time
        assertThat(columnMetas.get(10).getColumnName(), is("update_time"));
        assertThat(columnMetas.get(10).getColumnType(), is("timestamp"));
        assertThat(columnMetas.get(10).getGeneralColumnType(), is("timestamp"));
        assertThat(columnMetas.get(10).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(10).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat((columnMetas.get(10).getTypeEnum() == ColumnTypeEnum.MYSQL_TYPE_TIMESTAMP
                            || columnMetas.get(10).getTypeEnum() == ColumnTypeEnum.MYSQL_TYPE_TIMESTAMP2), is(
                is(true)));
        assertThat(columnMetas.get(10).getDefaultValue(), is("0000-00-00 00:00:00"));
        assertThat(columnMetas.get(10).getCharset(), nullValue());
        assertThat(columnMetas.get(10).getShortCharset(), nullValue());

        // ratio
        assertThat(columnMetas.get(11).getColumnName(), is("ratio"));
        assertThat(columnMetas.get(11).getColumnType(), is("float unsigned"));
        assertThat(columnMetas.get(11).getGeneralColumnType(), is("float"));
        assertThat(columnMetas.get(11).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(11).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(11).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_FLOAT));
        assertThat(columnMetas.get(11).getDefaultValue(), is("0"));
        assertThat(columnMetas.get(11).getCharset(), nullValue());
        assertThat(columnMetas.get(11).getShortCharset(), nullValue());
    }

    /**
     * 验证city表结构
     *
     * @param columnMetas
     */
    protected void validateTableDef4City(List<ColumnMeta> columnMetas) {
        assertThat(columnMetas != null, is(true));
        assertThat(columnMetas.size(), is(3));
        // id
        assertThat(columnMetas.get(0).getColumnName(), is("id"));
        assertThat(columnMetas.get(0).getColumnType(), is("bigint(20)"));
        assertThat(columnMetas.get(0).getGeneralColumnType(), is("bigint"));
        assertThat(columnMetas.get(0).getKeyFlag(), is(MetaDefine.KEY_FLAG.PRIMARY_KEY));
        assertThat(columnMetas.get(0).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(0).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_LONGLONG));
        assertThat(columnMetas.get(0).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(0).getCharset(), nullValue());
        assertThat(columnMetas.get(0).getShortCharset(), nullValue());

        // city_id
        assertThat(columnMetas.get(1).getColumnName(), is("city_id"));
        assertThat(columnMetas.get(1).getColumnType(), is("int(10)"));
        assertThat(columnMetas.get(1).getGeneralColumnType(), is("int"));
        assertThat(columnMetas.get(1).getKeyFlag(), is(MetaDefine.KEY_FLAG.UNIQUE_KEY));
        assertThat(columnMetas.get(1).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(1).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_LONG));
        assertThat(columnMetas.get(1).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(1).getCharset(), nullValue());
        assertThat(columnMetas.get(1).getShortCharset(), nullValue());

        // city_name
        assertThat(columnMetas.get(2).getColumnName(), is("city_name"));
        assertThat(columnMetas.get(2).getColumnType(), is("varchar(1024)"));
        assertThat(columnMetas.get(2).getGeneralColumnType(), is("varchar"));
        assertThat(columnMetas.get(2).getKeyFlag(), is(MetaDefine.KEY_FLAG.EMPTY));
        assertThat(columnMetas.get(2).getNullFlag(), is(MetaDefine.NOT_NULL_FLAG.NO));
        assertThat(columnMetas.get(2).getTypeEnum(), is(ColumnTypeEnum.MYSQL_TYPE_VARCHAR));
        assertThat(columnMetas.get(2).getDefaultValue(), nullValue());
        assertThat(columnMetas.get(2).getCharset(), is("utf8_general_ci"));
        assertThat(columnMetas.get(2).getShortCharset(), is("utf8"));
    }

    /**
     * 打印表结构
     *
     * @param changeDataSet
     * @param expectedTableNum 发生变化的表的数量
     */
    protected void printTableDef(ChangeDataSet changeDataSet, int expectedTableNum) {
        Map<String, List<ColumnMeta>> tableDef = changeDataSet.getTableDef();
        logger.info("TableDef: {}", tableDef);
        assertThat(tableDef.size(), is(expectedTableNum));
    }

    /**
     * 打印变化的数据
     *
     * @param changeDataSet
     * @param expectedTableNum 发生变化的表的数量
     */
    protected void printTableData(ChangeDataSet changeDataSet, int expectedTableNum) {
        Map<String, List<RowData>> tableData = changeDataSet.getTableData();
        for (String tableName : tableData.keySet()) {
            logger.info("TableName: " + tableName);
            for (RowData rowData : tableData.get(tableName)) {
                logger.info("Before:" + rowData.getBeforeColumnList());
                logger.info("After:" + rowData.getAfterColumnList());
            }
        }
        assertThat(tableData.size(), is(expectedTableNum));
    }

    /**
     * 持久化保存同步点
     *
     * @param ds 数据变化
     */
    protected void savePoint(ChangeDataSet ds) {
        if (bridge != null) {
            logger.info("Save point " + ds);
            bridge.getDisposeEventPosition(ds.getInstanceName()).saveSyncPoint(ds.getSyncPoint());
        }
    }

    /**
     * 验证同步点
     * <p/>
     * 默认文件名都是producer00，验证完之后删除
     *
     * @param ds           变化
     * @param contentRegex 文件的正则表达式
     */
    protected void validatePoint(ChangeDataSet ds, String contentRegex, String fileExt) {
        File file =
                new File(producerPositionPath + File.separator + "producer00" + (fileExt == null ? "" : "." + fileExt));
        try {
            String content = FileUtils.readFileToString(file);
            Pattern pattern = Pattern.compile(contentRegex);
            Matcher matcher = pattern.matcher(content);
            logger.info("regex is " + contentRegex + ", actual is " + content);
            assertThat(matcher.find(), is(true));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    @Value("${producer_position}")
    private String producerPositionPath;

    protected int getNeglectChangeDataSetNum() {
        return 0;
    }

}
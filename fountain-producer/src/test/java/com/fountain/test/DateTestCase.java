package com.fountain.test;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.eventposition.BaiduGroupIdSyncPoint;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.producer.dispatch.misc.BigChangeDataSetMessageSeparationPolicy;

/**
 * 简单功能测试用例
 */
public class DateTestCase {
    /**
     * 测试日期
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testString2DateWithCommon() {
        try {
            Date dt = DateUtils.parseDate("2014-01-31", "yyyy-MM-dd");
            Assert.assertTrue(dt.getYear() == 2014);
            Assert.assertTrue(dt.getMonth() == 1);
            Assert.assertTrue(dt.getDay() == 31);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试时间
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testString2TimeWithCommon() {
        try {
            Date dt = DateUtils.parseDate("14:30:22", "HH:mm:ss");
            Assert.assertTrue(dt.getHours() == 14);
            Assert.assertTrue(dt.getMinutes() == 30);
            Assert.assertTrue(dt.getSeconds() == 22);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试ChangeDataSet分隔
     */
    @Test
    public void testSparate() {
        ChangeDataSet ds =
                new ChangeDataSet(new BaiduGroupIdSyncPoint(
                        BigInteger.valueOf(100L)), "producer1");
        ds.setGtId(BigInteger.valueOf(100L));
        Map<String, List<ColumnMeta>> tableDef =
                new LinkedHashMap<String, List<ColumnMeta>>();
        List<ColumnMeta> table1 = new ArrayList<ColumnMeta>();
        ColumnMeta cm = new ColumnMeta();
        cm.setColumnName("id");
        table1.add(cm);
        tableDef.put("tabl1", table1);
        tableDef.put("tabl2", table1);

        Map<String, List<RowData>> tableData1 =
                new LinkedHashMap<String, List<RowData>>();
        List<RowData> table1Row = new ArrayList<RowData>();
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());
        table1Row.add(new RowData());

        List<RowData> table2Row = new ArrayList<RowData>();
        table2Row.add(new RowData());
        table2Row.add(new RowData());
        table2Row.add(new RowData());
        table2Row.add(new RowData());
        table2Row.add(new RowData());
        table2Row.add(new RowData());
        table2Row.add(new RowData());
        table2Row.add(new RowData());
        table2Row.add(new RowData());
        table2Row.add(new RowData());

        tableData1.put("tabl1", table1Row);
        tableData1.put("tabl2", table2Row);

        ds.setTableDef(tableDef);
        ds.setTableData(tableData1);
        ds.setDataSize(table1Row.size() + table2Row.size());
        BigChangeDataSetMessageSeparationPolicy policy =
                new BigChangeDataSetMessageSeparationPolicy();
        policy.setMaxSubSize(15);

        Iterator<Object> it = policy.separate(ds);
        List<Object> retList = new ArrayList<Object>();
        while (it.hasNext()) {
            Object o = it.next();
            retList.add(o);
        }
        Assert.assertTrue(retList.size() == 2);
    }
}

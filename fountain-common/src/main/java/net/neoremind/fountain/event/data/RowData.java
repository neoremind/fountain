package net.neoremind.fountain.event.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangxu
 */
public class RowData {
    /**
     * Variable-sized. A sequence of zero or more rows. The end is determined by the size of the event. Each row has the
     * following format:
     * <p/>
     * Variable-sized. Bit-field indicating whether each field in the row is NULL. Only columns that are "used"
     * according to the second field in the variable data part are listed here. If the second field in the variable data
     * part has N one-bits, the amount of storage required for this field is INT((N+7)/8) bytes.
     * <p/>
     * Variable-sized. The row-image, containing values of all table fields. This only lists table fields that are used
     * (according to the second field of the variable data part) and non-NULL (according to the previous field). In
     * other words, the number of values listed here is equal to the number of zero bits in the previous field (not
     * counting padding bits in the last byte).
     * <p/>
     * The format of each value is described in the log_event_print_value() function in log_event.cc.
     */
    // private static final int WRITE_ROWS_EVENT = 23;
    // private static final int UPDATE_ROWS_EVENT = 24;
    // private static final int DELETE_ROWS_EVENT = 25;

    // private int sqlType;
    private List<ColumnData> beforeColumnList = new ArrayList<ColumnData>();
    private List<ColumnData> afterColumnList = new ArrayList<ColumnData>();

    // public int getSqlType() {
    // return sqlType;
    // }
    // public void setSqlType(int sqlType) {
    // this.sqlType = sqlType;
    // }
    public List<ColumnData> getBeforeColumnList() {
        return beforeColumnList;
    }

    public void setBeforeColumnList(List<ColumnData> beforeColumnList) {
        this.beforeColumnList = beforeColumnList;
    }

    public List<ColumnData> getAfterColumnList() {
        return afterColumnList;
    }

    public void setAfterColumnList(List<ColumnData> afterColumnList) {
        this.afterColumnList = afterColumnList;
    }

    public boolean isWrite() {
        return beforeColumnList.size() == 0;
    }

    public boolean isUpdate() {
        return beforeColumnList.size() > 0 && afterColumnList.size() > 0;
    }

    public boolean isDelete() {
        return afterColumnList.size() == 0;
    }
}
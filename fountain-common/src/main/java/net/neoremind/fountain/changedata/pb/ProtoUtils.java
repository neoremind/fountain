package net.neoremind.fountain.changedata.pb;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.changedata.pb.DataUnitDef.DataSet;
import net.neoremind.fountain.changedata.pb.DataUnitDef.TableData;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.meta.ColumnMeta;
import com.google.protobuf.ByteString;

/**
 * changedata set to proto的工具类
 * 
 * @author hexiufeng
 * 
 */
public class ProtoUtils {
    private static final int OP_INSERT = 0;
    private static final int OP_UPDATE = 1;
    private static final int OP_DELETE = 2;

    private ProtoUtils() {

    }

    /**
     * 转化ChangeDataSet成pb格式
     * 
     * @param changeSet ChangeDataSet
     * @return pb format
     */
    public static DataSet convert2Pb(ChangeDataSet changeSet) {
        DataSet.Builder dsBuilder = DataUnitDef.DataSet.newBuilder();
        dsBuilder.setGtId(changeSet.getGtId() == null ? -1L : changeSet.getGtId().longValue());
        dsBuilder.addAllTables(extractTableData(changeSet));
        return dsBuilder.build();
    }

    /**
     * 抽取表数据到pb
     * 
     * @param changeSet ChangeDataSet
     * @return 表数据列表
     */
    private static List<TableData> extractTableData(ChangeDataSet changeSet) {
        List<TableData> list = new ArrayList<TableData>(changeSet.getTableDef().size());
        for (String tableName : changeSet.getTableData().keySet()) {
            List<RowData> rowList = changeSet.getTableData().get(tableName);
            TableData.Builder builder = TableData.newBuilder();
            builder.setName(tableName);
            builder.addAllColumnMeta(extractColumnMeta(changeSet.getTableDef().get(tableName)));
            for (RowData rd : rowList) {
                builder.addRows(extractRow(rd, changeSet.getTableDef().get(tableName)));
            }

            list.add(builder.build());
        }
        return list;
    }

    /**
     * 转化字段meta到pb格式
     * 
     * @param metaList meta info
     * @return pb format
     */
    private static List<DataUnitDef.ColumnMeta> extractColumnMeta(List<ColumnMeta> metaList) {
        List<DataUnitDef.ColumnMeta> list = new ArrayList<DataUnitDef.ColumnMeta>(metaList.size());
        for (ColumnMeta meta : metaList) {
            DataUnitDef.ColumnMeta.Builder builder = DataUnitDef.ColumnMeta.newBuilder();
            builder.setName(meta.getColumnName());
            builder.setDataType(meta.getTypeEnum().getTypeValue());
            builder.setIsNull(meta.isNullable());
            builder.setIsUnsiged(meta.isUnsigned());
            builder.setIsPrimay(false);
            list.add(builder.build());
        }
        return list;
    }

    /**
     * 抽取行数据到pb
     * 
     * @param originalRow 行数据
     * @param metaList 字段meta list
     * @return pb row
     */
    private static DataUnitDef.RowData extractRow(RowData originalRow, List<ColumnMeta> metaList) {
        DataUnitDef.RowData.Builder builder = DataUnitDef.RowData.newBuilder();
        if (originalRow.isWrite()) {
            builder.setOptype(OP_INSERT);
            builder.addAllAfterValues(extractColumnValues(originalRow.getAfterColumnList(), metaList));
        }
        if (originalRow.isUpdate()) {
            builder.setOptype(OP_UPDATE);
            builder.addAllAfterValues(extractColumnValues(originalRow.getAfterColumnList(), metaList));
            builder.addAllBeforeValues(extractColumnValues(originalRow.getBeforeColumnList(), metaList));
        }
        if (originalRow.isDelete()) {
            builder.setOptype(OP_DELETE);
            builder.addAllBeforeValues(extractColumnValues(originalRow.getBeforeColumnList(), metaList));
        }
        return builder.build();
    }

    /**
     * 抽取一行的所有列数据到pb
     * 
     * @param dataList 字段数据
     * @param metaList 字段meta
     * @return pb value list
     */
    private static List<DataUnitDef.ColumnValue> extractColumnValues(List<ColumnData> dataList,
            List<ColumnMeta> metaList) {
        List<DataUnitDef.ColumnValue> list = new ArrayList<DataUnitDef.ColumnValue>(metaList.size());
        int index = 0;
        for (ColumnData data : dataList) {
            DataUnitDef.ColumnValue.Builder builder = DataUnitDef.ColumnValue.newBuilder();
            ColumnMeta meta = metaList.get(index++);
            if (data.isNull()) {
                builder.setIsNull(true);
            } else {
                applyValue(builder, data.getValue(), meta);
            }
            list.add(builder.build());
        }
        return list;
    }

    /**
     * 设置pb column value
     * 
     * @param builder DataUnitDef.ColumnValue.Builder
     * @param value value
     * @param meta meta info
     */
    private static void applyValue(DataUnitDef.ColumnValue.Builder builder, Object value, ColumnMeta meta) {
        switch (meta.getTypeEnum()) {
            case MYSQL_TYPE_DECIMAL:
                builder.setStringValue(value.toString());
                break;
            case MYSQL_TYPE_NEWDECIMAL:
                builder.setStringValue(value.toString());
                break;

            case MYSQL_TYPE_TINY:
                builder.setIntValue((Byte) value);
                break;
            case MYSQL_TYPE_SHORT:
                builder.setIntValue((Integer) value);
                break;
            case MYSQL_TYPE_INT24:
                builder.setIntValue((Integer) value);
                break;
            case MYSQL_TYPE_LONG:
                if (value instanceof Long) {
                    builder.setIntValue(((Long) value).intValue());
                } else {
                    builder.setIntValue((Integer) value);
                }
                break;
            case MYSQL_TYPE_FLOAT:
                builder.setFloatValue((Float) value);
                break;
            case MYSQL_TYPE_DOUBLE:
                builder.setDoubleValue((Double) value);
                break;
            case MYSQL_TYPE_LONGLONG:
                if (value instanceof BigInteger) {
                    builder.setLongValue(((BigInteger) value).longValue());
                } else {
                    builder.setLongValue((Long) value);
                }
                break;
            case MYSQL_TYPE_DATE:
                builder.setLongValue(((Date) value).getTime());
                break;
            case MYSQL_TYPE_DATETIME:
                builder.setLongValue(((Date) value).getTime());
                break;
            case MYSQL_TYPE_NEWDATE:
                builder.setLongValue(((Date) value).getTime());
                break;
            case MYSQL_TYPE_VARCHAR:
                builder.setStringValue(value.toString());
                break;
            case MYSQL_TYPE_BIT:
                builder.setBoolValue((Boolean) value);
                break;
            case MYSQL_TYPE_STRING:
                builder.setStringValue(value.toString());
                break;
            case MYSQL_TYPE_TINY_BLOB:
                builder.setByteArrayValue(ByteString.copyFrom((byte[]) value));
                break;
            case MYSQL_TYPE_MEDIUM_BLOB:
                builder.setByteArrayValue(ByteString.copyFrom((byte[]) value));
                break;
            case MYSQL_TYPE_LONG_BLOB:
                builder.setByteArrayValue(ByteString.copyFrom((byte[]) value));
                break;
            case MYSQL_TYPE_BLOB:
                if (value instanceof String) {
                    builder.setStringValue(value.toString());
                } else {
                    builder.setByteArrayValue(ByteString.copyFrom((byte[]) value));
                }
                break;
            default:
                break;
        }
    }
}

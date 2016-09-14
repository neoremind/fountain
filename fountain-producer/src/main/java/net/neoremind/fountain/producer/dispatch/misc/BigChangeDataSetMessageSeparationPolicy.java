package net.neoremind.fountain.producer.dispatch.misc;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.meta.ColumnMeta;

/**
 * 分解大的datachangeset的策略,多用于多个producer数据发往同一mq
 *
 * @author hexiufeng
 */
public class BigChangeDataSetMessageSeparationPolicy implements
        MessageSeparationPolicy {
    /**
     * 不返回任何对象的迭代器
     */
    private static final Iterator<Object> NULL_IT = new Iterator<Object>() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }

        @Override
        public void remove() {

        }

    };
    /**
     * 分包中的最大数据条数
     */
    private int maxSubSize = 10;

    @Override
    public Iterator<Object> separate(final Object message) {
        if (!(message instanceof ChangeDataSet)) {
            return NULL_IT;
        }
        ChangeDataSet ds = (ChangeDataSet) message;
        if (ds.getDataSize() < maxSubSize) {
            return new SingleIterator(message);
        }
        return sparateCode(ds);
    }

    /**
     * 分解ChangeDataSet为多个
     *
     * @param ds
     *
     * @return
     */
    private Iterator<Object> sparateCode(final ChangeDataSet ds) {
        return new Iterator<Object>() {
            private int size = (ds.getDataSize() + maxSubSize - 1) / maxSubSize;
            private final String[] tableNameArray = ds.getTableDef().keySet()
                    .toArray(new String[0]);
            private int tableIndex = 0;
            private int rowIndex = 0;

            @Override
            public boolean hasNext() {
                return size > 0;
            }

            @Override
            public Object next() {
                ChangeDataSet sub = createDs(ds);

                size--;
                return sub;
            }

            /**
             * 从ChangeDataSet分解出子ChangeDataSet
             *
             * @param ds ChangeDataSet
             * @return 子ChangeDataSet
             */
            private ChangeDataSet createDs(final ChangeDataSet ds) {
                ChangeDataSet sub =
                        new ChangeDataSet(ds.getSyncPoint(),
                                ds.getInstanceName());
                copyDataSetCommonProps(ds, sub);

                int acceptCount = 0;
                Map<String, List<ColumnMeta>> tableDef =
                        new LinkedHashMap<String, List<ColumnMeta>>();
                Map<String, List<RowData>> tableData =
                        new LinkedHashMap<String, List<RowData>>();

                while (acceptCount < maxSubSize) {
                    ensureIndex(ds);
                    if (tableIndex >= tableNameArray.length) {
                        break;
                    }
                    String tableName = tableNameArray[tableIndex];
                    tableDef.put(tableName, ds.getTableDef().get(tableName));
                    List<RowData> tableList = new LinkedList<RowData>();
                    List<RowData> srcList = ds.getTableData().get(tableName);
                    int needCount = maxSubSize - acceptCount;
                    int end =
                            (srcList.size() - rowIndex) > needCount ? (rowIndex + needCount)
                                    : srcList.size();
                    for (; rowIndex < end; rowIndex++) {
                        tableList.add(srcList.get(rowIndex));
                        acceptCount++;
                    }
                    tableData.put(tableName, tableList);
                }

                sub.setTableDef(tableDef);
                sub.setTableData(tableData);
                sub.setDataSize(acceptCount);
                return sub;
            }

            /**
             * 保证表索引和表内记录索引的有效性
             *
             * @param ds ChangeDataSet
             */
            private void ensureIndex(final ChangeDataSet ds) {
                while (tableIndex < tableNameArray.length) {
                    String tableName = tableNameArray[tableIndex];
                    if (rowIndex >= ds.getTableData().get(tableName).size()) {
                        tableIndex++;
                        rowIndex = 0;
                    } else {
                        break;
                    }
                }
            }

            @Override
            public void remove() {

            }

        };
    }

    /**
     * copy通用的属性
     *
     * @param src  源对象
     * @param dest 目标对象
     */
    private void copyDataSetCommonProps(final ChangeDataSet src,
                                        final ChangeDataSet dest) {
        dest.setBirthTime(src.getBirthTime());
        dest.setGtId(src.getGtId());
        dest.setSendTime(src.getSendTime());
    }

    public int getMaxSubSize() {
        return maxSubSize;
    }

    public void setMaxSubSize(int maxSubSize) {
        this.maxSubSize = maxSubSize;
    }

}

package net.neoremind.fountain.changedata;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.meta.ColumnMeta;

/**
 * 描述变化的数据的数据结构，类似jdbc的RowsSet，包括元数据和数据变化。<br/>
 * 元数据包括:
 * <ul>
 * <li>1）Global transaction id，简称gtid</li>
 * <li>2）数据来源，指代生产者标示<code>instanceName</code>和数据源<code>IP:PORT</code></li>
 * <li>3）表schema描述,数据以Map&lt;String,List&lt;ColumnMeta&gt;&gt;的形式呈现，key表示表名，value是一个List，描述对应该表的数据,每一个元素是一个
 * {@link ColumnMeta ColumnMeta}对象</li>
 * </ul>
 * <p/>
 * 数据变化包括:
 * <ul>
 * <li>数据以Map&lt;String,List&lt;RowData&gt;&gt;的形式呈现，key表示表名，value是一个List，描述对应该表的数据,每一个元素是一个
 * {@link RowData RowData}对象，包括修改前后的数据详细信息。</li>
 * </ul>
 *
 * @author zhangxu
 */
public class ChangeDataSet implements BinlogTraceable {

    /**
     * Global transaction id,也称作gtid或者groupid
     * <p/>
     * 来自于写库,用于表示写库中的事务id,在mysql数据复制时会携带该id
     */
    private BigInteger gtId;

    /**
     * 生产者实例名称，代表一个producer线程的名称,一个producer线程绑定一个db,因此instanceName代表一个数据源
     */
    private String instanceName;

    /**
     * 表的schema描述, key表示表全名, value表示表的各个列的schema
     *
     * @see ColumnMeta
     */
    private Map<String, List<ColumnMeta>> tableDef = new LinkedHashMap<String, List<ColumnMeta>>();

    /**
     * 表的数据描述,key表示表全名, value表示表的行数据，包括前后变化
     *
     * @see RowData
     */
    private Map<String, List<RowData>> tableData = new LinkedHashMap<String, List<RowData>>();

    /**
     * Binglog event header中的时间戳，从unix epoch以来的秒数
     *
     * @see <a href="http://dev.mysql.com/doc/internals/en/binlog-event-header.html">binlog-event-header</a>
     */
    private long birthTime;

    /**
     * Fountain-producer下发<code>ChangeDataSet</code>生产的时间戳，从unix epoch以来的秒数
     */
    private long sendTime;

    /**
     * 同步点
     */
    private transient SyncPoint syncPoint;

    /**
     * 数据库来源，用IP:PORT表示
     */
    private String srcDbHost;

    /**
     * 对应mysql binlog的bytes len
     */
    private int dataSize = 0;

    /**
     * 构造方法
     */
    public ChangeDataSet() {
        syncPoint = null;
    }

    /**
     * 构造方法
     *
     * @param syncPoint    同步点
     * @param instanceName 生产者实例名称
     */
    public ChangeDataSet(SyncPoint syncPoint, String instanceName) {
        this.syncPoint = syncPoint;
        this.instanceName = instanceName;
    }

    /**
     * 根据表的全名获取列的信息,表的全名比如beidou.cproplan
     *
     * @param tableFullName
     *
     * @return
     */
    public List<ColumnMeta> getColumnMetaByFullTableName(String tableFullName) {
        return tableDef.get(tableFullName);
    }

    /**
     * 根据表的全名获取表的数据,表的全名比如beidou.cproplan
     *
     * @param tableFullName
     *
     * @return
     */
    public List<RowData> getTableRowDatasByFullTableName(String tableFullName) {
        return tableData.get(tableFullName);
    }

    /**
     * 获取所有的表名，表名为全名,比如beidou.cproplan
     *
     * @return
     */
    public List<String> getTableNameList() {
        List<String> nameList = new ArrayList<String>(tableDef.size());
        nameList.addAll(tableDef.keySet());
        return nameList;
    }

    @Override
    public String toString() {
        return "ChangeDataSet{" +
                "gtId=" + gtId +
                ", instanceName='" + instanceName + '\'' +
                ", srcDbHost='" + srcDbHost + '\'' +
                ", dataSize=" + dataSize +
                ", syncPoint=" + syncPoint +
                ", birthTime=" + birthTime +
                ", sendTime=" + sendTime +
                '}';
    }

    @Override
    public SyncPoint getSyncPoint() {
        return syncPoint;
    }

    @Override
    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public void setSyncPoint(SyncPoint syncPoint) {
        this.syncPoint = syncPoint;
    }

    public String getSrcDbHost() {
        return srcDbHost;
    }

    public void setSrcDbHost(String srcDbHost) {
        this.srcDbHost = srcDbHost;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public BigInteger getGtId() {
        return gtId;
    }

    public void setGtId(BigInteger gtId) {
        this.gtId = gtId;
    }

    public Map<String, List<ColumnMeta>> getTableDef() {
        return tableDef;
    }

    public void setTableDef(Map<String, List<ColumnMeta>> tableDef) {
        this.tableDef = tableDef;
    }

    public Map<String, List<RowData>> getTableData() {
        return tableData;
    }

    public void setTableData(Map<String, List<RowData>> tableData) {
        this.tableData = tableData;
    }

    public long getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(long birthTime) {
        this.birthTime = birthTime;
    }
}

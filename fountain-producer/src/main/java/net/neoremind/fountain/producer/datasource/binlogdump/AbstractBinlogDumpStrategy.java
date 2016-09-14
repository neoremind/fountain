package net.neoremind.fountain.producer.datasource.binlogdump;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.datasource.MysqlDataSource;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.exception.DataErrorException;
import net.neoremind.fountain.packet.ResultSetPacket;
import net.neoremind.fountain.packet.RowValuePacket;
import net.neoremind.fountain.producer.exception.UnsupportedBinlogDumpException;
import net.neoremind.fountain.support.ThreadHolder;
import net.neoremind.fountain.rowbaselog.event.RotateEvent;
import net.neoremind.fountain.util.CollectionUtils;
import net.neoremind.fountain.util.MysqlCommonConstants;
import net.neoremind.fountain.rowbaselog.event.RowsLogEvent;

/**
 * binlog dump策略接口抽象类，子类泛化成为具体的实施办法
 *
 * @author zhangxu
 * @see BinlogDumpStrategy
 */
public abstract class AbstractBinlogDumpStrategy implements BinlogDumpStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBinlogDumpStrategy.class);

    /**
     * 查询版本sql
     */
    protected static final String QUERY_MYSQL_VERSION = "select version();";

    /**
     * 查询gtid_mode模式sql
     *
     * @see MysqlCommonConstants.GtIdMode
     */
    protected static final String QUERY_GTID_MODE = "show global variables like 'gtid_mode';";

    /**
     * 查询binlog format的sql
     *
     * @see MysqlCommonConstants.RowFormat
     */
    protected static final String QUERY_BIGLOG_FORMAT = "show variables like 'binlog_format';";

    /**
     * 是否支持checksum
     * <p/>
     * 一般来说如果启用了checksum，则每个{@link RowsLogEvent}后面都会多出来4个byte，
     * 以及{@link RotateEvent}中的binlog filename也会多出4个byte。
     */
    protected boolean isChecksumSupport = false;

    /**
     * 检查是否能用这个策略做binlog dump
     *
     * @param dataSource               数据源
     * @param queryValidationCallbacks 查询验证的回调
     *
     * @return 是否可以做binlog dump
     *
     * @throws UnsupportedBinlogDumpException
     */
    protected boolean doIsSupport(MysqlDataSource dataSource, QueryValidationCallback... queryValidationCallbacks)
            throws UnsupportedBinlogDumpException {
        if (queryValidationCallbacks == null) {
            return true;
        }

        try {
            for (QueryValidationCallback queryValidationCallback : queryValidationCallbacks) {
                ResultSetPacket resultSetPacket = dataSource.query(queryValidationCallback.getQuerySql());
                if (resultSetPacket == null) {
                    throw new IOException("Can not query mysql for " + queryValidationCallback.getQuerySql());
                }

                List<RowValuePacket> rowValueList = resultSetPacket.getRowValueList();
                if (CollectionUtils.isEmpty(rowValueList)) {
                    throw new DataErrorException(
                            "Query mysql error, do not get row value");
                }

                RowValuePacket rowPacket = rowValueList.get(0);
                if (rowPacket == null
                        || CollectionUtils.isEmpty(rowPacket.getFieldValueList())
                        || rowPacket.getFieldValueList().size() != queryValidationCallback.getExpectedFieldNumber()) {
                    throw new DataErrorException(
                            "Query mysql error, field format is not valid");
                }

                Pattern pattern = Pattern.compile(queryValidationCallback.getCheckFieldRegex());
                String result = rowPacket.getFieldValueList().get(queryValidationCallback.getCheckFieldIndex());
                Matcher matcher = pattern.matcher(result);
                logger.info(queryValidationCallback.getQuerySql() + " result is " + result);
                if (!matcher.find()) {
                    throw new DataErrorException("Check failed: " + queryValidationCallback.getCheckFieldIndex()
                            + " does not match BinlogDumpStrategy specified: " + queryValidationCallback
                            .getCheckFieldRegex() + " for query: " + queryValidationCallback.getQuerySql());
                }
            }
        } catch (Exception e) {
            throw new UnsupportedBinlogDumpException(e);
        }

        return true;
    }

    /**
     * 用于做验证查询sql的回调
     */
    interface QueryValidationCallback {

        /**
         * 查询sql
         *
         * @return sql
         */
        String getQuerySql();

        /**
         * 预期结果的字段数量
         *
         * @return 字段数量
         */
        int getExpectedFieldNumber();

        /**
         * 检查哪个字段
         *
         * @return 待检查字段
         */
        int getCheckFieldIndex();

        /**
         * 检查字段的正则表达式
         *
         * @return 正则表达式
         */
        String getCheckFieldRegex();

    }

    /**
     * 验证MySQL版本
     */
    abstract class MySQLVersionValidationCallback implements QueryValidationCallback {
        @Override
        public String getQuerySql() {
            return QUERY_MYSQL_VERSION;
        }

        @Override
        public int getExpectedFieldNumber() {
            return 1;
        }

        @Override
        public int getCheckFieldIndex() {
            return 0;
        }
    }

    /**
     * 验证gtid模式是否开启
     */
    class GtIdModeValidationCallback implements QueryValidationCallback {
        @Override
        public String getQuerySql() {
            return QUERY_GTID_MODE;
        }

        @Override
        public int getExpectedFieldNumber() {
            return 2;
        }

        @Override
        public int getCheckFieldIndex() {
            return 1;
        }

        @Override
        public String getCheckFieldRegex() {
            return MysqlCommonConstants.GtIdMode.ON.getValue();
        }
    }

    /**
     * 检查binlog format是否是ROW BASED
     */
    class BinlogRowFormatValidationCallback implements QueryValidationCallback {
        @Override
        public String getQuerySql() {
            return QUERY_BIGLOG_FORMAT;
        }

        @Override
        public int getExpectedFieldNumber() {
            return 2;
        }

        @Override
        public int getCheckFieldIndex() {
            return 1;
        }

        @Override
        public String getCheckFieldRegex() {
            return MysqlCommonConstants.RowFormat.BINLOG_FORMAT_ROW.getValue();
        }
    }

    @Override
    public SyncPoint transformSyncPoint(SyncPoint syncPoint, MysqlDataSource dataSource)
            throws IOException, NoSuchAlgorithmException {
        return syncPoint;
    }

    @Override
    public boolean isChecksumSupport() {
        if (isChecksumSupport) {
            ThreadHolder.getTrxContext().setIsChecksumSupport(true);
        }
        return isChecksumSupport;
    }

    public void setIsChecksumSupport(boolean isChecksumSupport) {
        this.isChecksumSupport = isChecksumSupport;
    }
}

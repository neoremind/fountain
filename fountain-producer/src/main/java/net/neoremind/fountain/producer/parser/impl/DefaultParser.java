package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.event.ColumnDataParserFactory;
import net.neoremind.fountain.event.EventHeader;
import net.neoremind.fountain.event.util.TableEventCache;
import net.neoremind.fountain.exception.DataErrorException;
import net.neoremind.fountain.exception.EOFException;
import net.neoremind.fountain.meta.TableMeta;
import net.neoremind.fountain.meta.TableMetaProvider;
import net.neoremind.fountain.packet.EOFPacket;
import net.neoremind.fountain.packet.ErrorPacket;
import net.neoremind.fountain.producer.matcher.EventMatcher;
import net.neoremind.fountain.producer.parser.Parser;
import net.neoremind.fountain.rowbaselog.event.BinlogEventHeader;
import net.neoremind.fountain.rowbaselog.event.EventConstant;
import net.neoremind.fountain.rowbaselog.event.FormatDescriptionEvent;
import net.neoremind.fountain.rowbaselog.event.FormatInfo;
import net.neoremind.fountain.rowbaselog.event.FormatInfoCallback;
import net.neoremind.fountain.rowbaselog.event.GtidEvent;
import net.neoremind.fountain.rowbaselog.event.QueryLogEvent;
import net.neoremind.fountain.rowbaselog.event.RotateEvent;
import net.neoremind.fountain.rowbaselog.event.RowsLogEvent;
import net.neoremind.fountain.rowbaselog.event.RowsLogEventV1;
import net.neoremind.fountain.rowbaselog.event.RowsLogEventV2;
import net.neoremind.fountain.rowbaselog.event.StopEvent;
import net.neoremind.fountain.rowbaselog.event.TableEventCallback;
import net.neoremind.fountain.rowbaselog.event.TableInfoCallback;
import net.neoremind.fountain.rowbaselog.event.TableMapEvent;
import net.neoremind.fountain.rowbaselog.event.XidLogEvent;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class DefaultParser implements Parser {
    private static final Logger logger = LoggerFactory
            .getLogger(DefaultParser.class);
    private FormatInfo fmtInfo = new FormatInfo() {

        @Override
        public int getExtraHeadersLength() {
            return 0;
        }

        @Override
        public int getPostHeaderLen(int eventType) {
            return 0;
        }

    };
    private TableEventCache tableEventCache = new TableEventCache();
    private TableMetaProvider tableMetaProvider;
    private final EventMatcher tableNameMatcher;
    private ColumnDataParserFactory columnDataParserFactory =
            new DefaultColumnDataParserFactory();
    private final TableInfoCallback tableInfoCallback =
            new TableInfoCallback() {

                @Override
                public TableMapEvent getTableMapEvent(long tableId) {
                    return tableEventCache.getTableMapEvent(tableId);
                }

                @Override
                public TableMeta getTableMeta(String tableName, long tableId,
                                              TableMapEvent.ColumnInfo[] columnInfo) {
                    TableMeta tableMeta =
                            tableMetaProvider.getTableMeta(tableName, tableId);
                    if (tableMeta == null) {
                        return null;
                    }
                    for (int i = 0; i < columnInfo.length; i++) {
                        if (columnInfo[i].type != tableMeta.getColumnMetaList()
                                .get(i).getTypeEnum()) {
                            tableMeta.getColumnMetaList().get(i)
                                    .setTypeEnum(columnInfo[i].type);
                        }
                    }
                    return tableMeta;
                }

            };

    public DefaultParser() {
        this(new EventMatcher() {

            @Override
            public boolean matcher(BaseLogEvent event) {
                return true;
            }

        });
    }

    public DefaultParser(EventMatcher tableNameMatcher) {
        this.tableNameMatcher = tableNameMatcher;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EventHeader> T parseHeader(ByteBuffer buf) {
        judgeEventError(buf);
        EventHeader header =
                new BinlogEventHeader(fmtInfo.getExtraHeadersLength());
        header.parseHeader(buf);
        return (T) header;
    }

    @Override
    public <T> BaseLogEvent parseDataToEvent(ByteBuffer buf, T header) {
        BaseLogEvent event = eventFactory((BinlogEventHeader) header);
        if (event == null) {
            return null;
        }
        return event.parseData(buf);
    }

    @Override
    public void setTableMetaProvider(TableMetaProvider tableMetaProvider) {
        this.tableMetaProvider = tableMetaProvider;
    }

    private void judgeEventError(ByteBuffer buff) {

        int flag =
                (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buff,
                        1);

        if (flag != 0) {
            if (ErrorPacket.isErrorPacket(buff.array())) {
                ErrorPacket errorPacket = new ErrorPacket();
                errorPacket.fromBytes(buff.array());
                throw new DataErrorException(new StringBuilder(
                        "Receive Error Packet! Error code is ")
                        .append(errorPacket.getErrorCode())
                        .append(", sqlstateMarker is ")
                        .append(errorPacket.getSqlstateMarker())
                        .append(", sqlstate is ")
                        .append(Arrays.deepToString(ArrayUtils
                                .toObject(errorPacket.getSqlstate())))
                        .append(", error message is ")
                        .append(errorPacket.getMessage()).toString());
            } else if (EOFPacket.isEOFPacket(buff.array())) {
                EOFPacket eofPacket = new EOFPacket();
                eofPacket.fromBytes(buff.array());
                throw new EOFException(
                        "Receive EOF Packet! May be Mysql is closed or you have another client with the same slaveId "
                                + "connected! Warning count is "
                                + eofPacket.getWarningCount()
                                + ", statusFlags is "
                                + Arrays.deepToString(ArrayUtils
                                .toObject(eofPacket.getStatusFlags())));
            } else {
                throw new DataErrorException(
                        "Receive abnormal Packet, flag is " + flag);
            }
        }

        return;
    }

    private boolean isFilter(TableMapEvent tableMapEvent) {
        return tableNameMatcher.matcher(tableMapEvent);
    }

    private BaseLogEvent eventFactory(BinlogEventHeader eventHeader) {
        switch (eventHeader.getTypeCode()) {
            case EventConstant.ROTATE_EVENT: {
                return new RotateEvent(eventHeader);
            }
            case EventConstant.QUERY_EVENT: {
                return new QueryLogEvent(eventHeader);
            }
            case EventConstant.FORMAT_DESCRIPTION_EVENT: {
                return new FormatDescriptionEvent(eventHeader,
                        new FormatInfoCallback() {
                            @Override
                            public void accept(FormatInfo formatInfo) {
                                fmtInfo = formatInfo;
                            }
                        });
            }
            case EventConstant.TABLE_MAP_EVENT: {
                return new TableMapEvent(eventHeader, new TableEventCallback() {
                    @Override
                    public void acceptTableMapEvent(TableMapEvent tableMapEvent) {
                        if (isFilter(tableMapEvent)) {
                            tableEventCache.setTableMapEvent(tableMapEvent);
                        }
                    }
                });
            }
            case EventConstant.WRITE_ROWS_EVENT_V0:
            case EventConstant.UPDATE_ROWS_EVENT_V0:
            case EventConstant.DELETE_ROWS_EVENT_V0: {
                return new RowsLogEvent(eventHeader,
                        fmtInfo.getPostHeaderLen(eventHeader.getTypeCode()),
                        eventHeader.getTypeCode(), tableInfoCallback,
                        columnDataParserFactory);
            }
            case EventConstant.WRITE_ROWS_EVENT_V1:
            case EventConstant.UPDATE_ROWS_EVENT_V1:
            case EventConstant.DELETE_ROWS_EVENT_V1: {
                return new RowsLogEventV1(eventHeader,
                        fmtInfo.getPostHeaderLen(eventHeader.getTypeCode()),
                        eventHeader.getTypeCode(), tableInfoCallback,
                        columnDataParserFactory);
            }
            case EventConstant.WRITE_ROWS_EVENT_V2:
            case EventConstant.UPDATE_ROWS_EVENT_V2:
            case EventConstant.DELETE_ROWS_EVENT_V2: {
                return new RowsLogEventV2(eventHeader,
                        fmtInfo.getPostHeaderLen(eventHeader.getTypeCode()),
                        eventHeader.getTypeCode(), tableInfoCallback,
                        columnDataParserFactory);
            }
            case EventConstant.GTID_EVENT: {
                return new GtidEvent(eventHeader);
            }
            case EventConstant.XID_EVENT: {
                return new XidLogEvent(eventHeader);
            }
            case EventConstant.STOP_EVENT: {
                return new StopEvent(eventHeader);
            }
            case EventConstant.LOAD_EVENT: // used for load data infile in mysql
                // 3.23
            case EventConstant.NEW_LOAD_EVENT: // allow multile-character
                // strings as separators for load
                // data in file
            case EventConstant.CREATE_FILE_EVENT: // added in mysql 4.0
            case EventConstant.APPEND_BLOCK_EVENT: // added in mysql 4.0
            case EventConstant.EXEC_LOAD_EVENT: // added in mysql 4.0
            case EventConstant.DELETE_FILE_EVENT: // added in mysql 4.0
            case EventConstant.BEGIN_LOAD_QUERY_EVENT: // added in mysql 5.0.3
            case EventConstant.EXECUTE_LOAD_QUERY_EVENT: // added in mysql 5.0.3
            case EventConstant.INTVAR_EVENT:
            case EventConstant.RAND_EVENT:
            case EventConstant.USER_VAR_EVENT:
            case EventConstant.HEARTBEAT_LOG_EVENT: // added in mysql 5.6, do
            case EventConstant.IGNORABLE_LOG_EVENT: // added in mysql 5.6, do
            case EventConstant.ROWS_QUERY_LOG_EVENT: // added in mysql 5.6, do
            case EventConstant.ANONYMOUS_GTID_LOG_EVENT: // added in mysql 5.6,
            case EventConstant.PREVIOUS_GTIDS_LOG_EVENT: // added in mysql 5.6,
            case EventConstant.SLAVE_EVENT: // reserved for furture use
            case EventConstant.START_EVENT_V3: // appear in mysql 4 or earlier
            case EventConstant.INCIDENT_EVENT:
            case EventConstant.UNKNOWN_EVENT: // unkown
            default: {
                logger.debug(new StringBuilder(
                        "Omit this event, typeId is ")
                        .append(eventHeader.getTypeCode())
                        .append(", type is ")
                        .append(EventConstant.getTypeString(eventHeader.getTypeCode()))
                        .append(", nextPosition is ")
                        .append(eventHeader.getNextPosition())
                        .append(", time is ")
                        .append(eventHeader.getTimestamp()).toString());

                return null;
            }
        }
    }

    public ColumnDataParserFactory getColumnDataParserFactory() {
        return columnDataParserFactory;
    }

    public void setColumnDataParserFactory(
            ColumnDataParserFactory columnDataParserFactory) {
        this.columnDataParserFactory = columnDataParserFactory;
    }
}

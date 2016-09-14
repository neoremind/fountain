package net.neoremind.fountain.rowbaselog.event;

/**
 * @author hanxu03
 * @since 2013-7-16
 */
public class EventConstant {

    public final static int bitArray[] = {
            1,
            1 << 1,
            1 << 2,
            1 << 3,
            1 << 4,
            1 << 5,
            1 << 6,
            1 << 7
    };
    /**
     * event 的类型
     */
    public static final int UNKNOWN_EVENT = 0;
    public static final int START_EVENT_V3 = 1;
    public static final int QUERY_EVENT = 2;
    public static final int STOP_EVENT = 3;
    public static final int ROTATE_EVENT = 4;
    public static final int INTVAR_EVENT = 5;
    public static final int LOAD_EVENT = 6;
    public static final int SLAVE_EVENT = 7;
    public static final int CREATE_FILE_EVENT = 8;
    public static final int APPEND_BLOCK_EVENT = 9;
    public static final int EXEC_LOAD_EVENT = 10;
    public static final int DELETE_FILE_EVENT = 11;
    public static final int NEW_LOAD_EVENT = 12;
    public static final int RAND_EVENT = 13;
    public static final int USER_VAR_EVENT = 14;
    public static final int FORMAT_DESCRIPTION_EVENT = 15;
    public static final int XID_EVENT = 16;
    public static final int BEGIN_LOAD_QUERY_EVENT = 17;
    public static final int EXECUTE_LOAD_QUERY_EVENT = 18;
    public static final int TABLE_MAP_EVENT = 19;
    public static final int WRITE_ROWS_EVENT_V0 = 20;
    public static final int UPDATE_ROWS_EVENT_V0 = 21;
    public static final int DELETE_ROWS_EVENT_V0 = 22;
    public static final int WRITE_ROWS_EVENT_V1 = 23;
    public static final int UPDATE_ROWS_EVENT_V1 = 24;
    public static final int DELETE_ROWS_EVENT_V1 = 25;
    public static final int INCIDENT_EVENT = 26;
    public static final int HEARTBEAT_LOG_EVENT = 27;
    public static final int IGNORABLE_LOG_EVENT = 28;
    public static final int ROWS_QUERY_LOG_EVENT = 29;
    public static final int WRITE_ROWS_EVENT_V2 = 30;
    public static final int UPDATE_ROWS_EVENT_V2 = 31;
    public static final int DELETE_ROWS_EVENT_V2 = 32;
    public static final int GTID_EVENT = 33;
    public static final int ANONYMOUS_GTID_LOG_EVENT = 34;
    public static final int PREVIOUS_GTIDS_LOG_EVENT = 35;

    /**
     * variable 的类型
     */
    public static final byte Q_FLAGS2_CODE = 0;
    public static final byte Q_SQL_MODE_CODE = 1;
    public static final byte Q_CATALOG_CODE = 2;
    public static final byte Q_AUTO_INCREMENT = 3;
    public static final byte Q_CHARSET_CODE = 4;
    public static final byte Q_TIME_ZONE_CODE = 5;
    public static final byte Q_CATALOG_NZ_CODE = 6;
    public static final byte Q_LC_TIME_NAMES_CODE = 7;
    public static final byte Q_CHARSET_DATABASE_CODE = 8;
    public static final byte Q_TABLE_MAP_FOR_UPDATE_CODE = 9;

    public static String getTypeString(int typeCode) {
        switch (typeCode) {
            case 0:
                return "UNKNOWN_EVENT";
            case 1:
                return "START_EVENT_V3";
            case 2:
                return "QUERY_EVENT";
            case 3:
                return "STOP_EVENT";
            case 4:
                return "ROTATE_EVENT";
            case 5:
                return "INTVAR_EVENT";
            case 6:
                return "LOAD_EVENT";
            case 7:
                return "SLAVE_EVENT";
            case 8:
                return "CREATE_FILE_EVENT";
            case 9:
                return "APPEND_BLOCK_EVENT";
            case 10:
                return "EXEC_LOAD_EVENT";
            case 11:
                return "DELETE_FILE_EVENT";
            case 12:
                return "NEW_LOAD_EVENT";
            case 13:
                return "RAND_EVENT";
            case 14:
                return "USER_VAR_EVENT";
            case 15:
                return "FORMAT_DESCRIPTION_EVENT";
            case 16:
                return "XID_EVENT";
            case 17:
                return "BEGIN_LOAD_QUERY_EVENT";
            case 18:
                return "EXECUTE_LOAD_QUERY_EVENT";
            case 19:
                return "TABLE_MAP_EVENT";
            case 20:
                return "PRE_GA_WRITE_ROWS_EVENT";
            case 21:
                return "PRE_GA_UPDATE_ROWS_EVENT";
            case 22:
                return "PRE_GA_DELETE_ROWS_EVENT";
            case 23:
                return "WRITE_ROWS_EVENT_V1";
            case 24:
                return "UPDATE_ROWS_EVENT_V1";
            case 25:
                return "DELETE_ROWS_EVENT_V1";
            case 26:
                return "INCIDENT_EVENT";
            case 27:
                return "HEARTBEAT_LOG_EVENT";
            case 30:
                return "WRITE_ROWS_EVENT";
            case 31:
                return "UPDATE_ROWS_EVENT";
            case 32:
                return "DELETE_ROWS_EVENT";
            case 33:
                return "GTID_EVENT";
            case 34:
                return "ANONYMOUS_GTID_LOG_EVENT";
            case 35:
                return "PREVIOUS_GTIDS_LOG_EVENT";
            default:
                return null;
        }
    }

    public static boolean isInsert(byte typeCode) {
        return typeCode == EventConstant.WRITE_ROWS_EVENT_V0
                || typeCode == EventConstant.WRITE_ROWS_EVENT_V1
                || typeCode == EventConstant.WRITE_ROWS_EVENT_V2;
    }

    public static boolean isDelete(byte typeCode) {
        return typeCode == EventConstant.DELETE_ROWS_EVENT_V0
                || typeCode == EventConstant.DELETE_ROWS_EVENT_V1
                || typeCode == EventConstant.DELETE_ROWS_EVENT_V2;
    }

    public static boolean isUpdate(byte typeCode) {
        return typeCode == EventConstant.UPDATE_ROWS_EVENT_V0
                || typeCode == EventConstant.UPDATE_ROWS_EVENT_V1
                || typeCode == EventConstant.UPDATE_ROWS_EVENT_V2;
    }

}

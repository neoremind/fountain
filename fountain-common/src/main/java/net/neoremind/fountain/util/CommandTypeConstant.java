package net.neoremind.fountain.util;

/**
 * MySQL命名定义字典
 *
 * @author hanxu, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/command-phase.html">Command Phase</a>
 * <p/>
 * 2013年8月3日
 */
public class CommandTypeConstant {

    public static final byte COM_SLEEP = 0X00;
    public static final byte COM_QUIT = 0X01;
    public static final byte COM_INIT_DB = 0X02;
    public static final byte COM_QUERY = 0X03;
    public static final byte COM_FIELD_LIST = 0X04;
    public static final byte COM_CREATE_DB = 0X05;
    public static final byte COM_DROP_DB = 0X06;
    public static final byte COM_REFRESH = 0X07;
    public static final byte COM_SHUTDOWN = 0X08;
    public static final byte COM_STATISTICS = 0X09;
    public static final byte COM_PROCESS_INFO = 0X0A;
    public static final byte COM_CONNECT = 0X0B;
    public static final byte COM_PROCESS_KILL = 0X0C;
    public static final byte COM_DEBUG = 0X0D;
    public static final byte COM_PING = 0X0E;
    public static final byte COM_TIME = 0X0F;
    public static final byte COM_DELAYED_INSERT = 0X10;
    public static final byte COM_CHANGE_USER = 0X11;
    public static final byte COM_BINLOG_DUMP = 0X12;
    public static final byte COM_TABLE_DUMP = 0X13;
    public static final byte COM_CONNECT_OUT = 0X14;
    public static final byte COM_REGISTER_SLAVE = 0X15;
    public static final byte COM_STMT_PREPARE = 0X16;
    public static final byte COM_STMT_EXECUTE = 0X17;
    public static final byte COM_STMT_SEND_LONG_DATA = 0X18;
    public static final byte COM_STMT_CLOSE = 0X19;
    public static final byte COM_STMT_RESET = 0X1A;
    public static final byte COM_SET_OPTION = 0X1B;
    public static final byte COM_STMT_FETCH = 0X1C;
    public static final byte COM_DAEMON = 0X1D;
    public static final byte COM_BINLOG_DUMP_GTID = 0X1E;
}

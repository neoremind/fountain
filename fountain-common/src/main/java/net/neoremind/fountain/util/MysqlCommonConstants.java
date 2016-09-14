package net.neoremind.fountain.util;

/**
 * MySQL默认的一些静态变量
 *
 * @author hanxu03, zhangxu
 * @since 2013-7-10
 */
public class MysqlCommonConstants {

    /**
     * 客户端和服务端交换的Packet的最大字节数，即16MB
     */
    public static final int MAX_PACKET_LENGTH = (1 << 24);

    /**
     * Strings that are terminated by a [00] byte.
     */
    public static final int NULL_TERMINATED_STRING_DELIMITER = 0x00;

    /**
     * 默认客户端字符集
     * <p/>
     * <table border="1">
     * <tr>
     * <th>Number</th>
     * <th>Hex</th>
     * <th>Character Set Name</th>
     * </tr>
     * <tr>
     * <td>33</td>
     * <td>0x21</td>
     * <td>utf8_general_ci</td>
     * </tr>
     * </table>
     */
    public static final byte CLIENT_CHARSET_NUMBER = 33;

    /**
     * MySQL binlog复制的格式
     * <ul>
     * <li>ROW</li>
     * <li>STATEMENT</li>
     * <li>MIXED</li>
     * </ul>
     */
    public static enum RowFormat {

        /**
         * STATEMENT
         */
        BINLOG_FORMAT_STATEMENT("STATEMENT"),

        /**
         * ROW
         */
        BINLOG_FORMAT_ROW("ROW"),

        /**
         * MIXED
         */
        BINLOG_FORMAT_MIXED("MIXED");

        private RowFormat(final String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * GtId Mode状态
     * <ul>
     * <li>ON</li>
     * <li>OFF</li>
     * </ul>
     */
    public static enum GtIdMode {

        /**
         * STATEMENT
         */
        ON("ON"),

        /**
         * ROW
         */
        OFF("OFF");

        private GtIdMode(final String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

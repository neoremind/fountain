package net.neoremind.fountain.packet;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import net.neoremind.fountain.exception.ParamErrorException;
import net.neoremind.fountain.util.ProtocolHelper;

/**
 * mysql error packet
 * <p/>
 * 错误信息和错误代码请参考：
 * <a href="http://dev.mysql.com/doc/refman/5.7/en/error-messages-server.html>error-messages-server.html</a>
 *
 * @author hanxu03, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html">ERR_Packet</a>
 * @since 2013-7-11
 */
public class ErrorPacket extends MysqlPacket {

    private static final long serialVersionUID = -8788492242358140571L;

    /**
     * Error packet第一个字节为0xff
     */
    public static final byte ERROR_FLAG = (byte) 0xff;

    /**
     * [ff] header of the ERR packet
     *
     * @see #ERROR_FLAG
     */
    private byte errorFlag;

    /**
     * error-code
     */
    private int errorCode;

    /**
     * marker of the SQL State
     */
    private byte sqlstateMarker;

    /**
     * SQL State
     */
    private byte[] sqlstate;

    /**
     * human readable error message
     */
    private String message;

    /**
     * 给定的数据包是否为error数据包
     *
     * @param data 数据包
     *
     * @return boolean
     */
    public static boolean isErrorPacket(byte[] data) {
        if (data == null || data.length == 0) {
            throw new ParamErrorException("data is not valid");
        }

        return data[0] == ERROR_FLAG;
    }

    @Override
    public void fromBytes(byte[] data) {
        if (data == null || data.length == 0) {
            throw new ParamErrorException("data is not valid");
        }

        Position position = new Position();

        // 1 0xFF(1)
        errorFlag = data[position.getPosition()];
        position.increase();

        // 2 error code(2)
        errorCode = (int) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 2);

        // 3 sqlstate marker(1)
        sqlstateMarker = data[position.getPosition()];
        position.increase();

        // 4 sqlstate(5)
        sqlstate = ProtocolHelper.getFixedBytes(data, position, 5);

        // 5 message
        message = new String(ProtocolHelper.getFixedBytes(data, position, data.length - position.getPosition()));
    }

    public byte getErrorFlag() {
        return errorFlag;
    }

    public void setErrorFlag(byte errorFlag) {
        this.errorFlag = errorFlag;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public byte getSqlstateMarker() {
        return sqlstateMarker;
    }

    public void setSqlstateMarker(byte sqlstateMarker) {
        this.sqlstateMarker = sqlstateMarker;
    }

    public byte[] getSqlstate() {
        return sqlstate;
    }

    public void setSqlstate(byte[] sqlstate) {
        this.sqlstate = sqlstate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("errorCode", errorCode)
                .append("errorFlag", errorFlag)
                .append("sqlstateMarker", sqlstateMarker)
                .append("sqlstate", Arrays.deepToString(ArrayUtils.toObject(sqlstate)))
                .append("message", message)
                .toString();
    }
}

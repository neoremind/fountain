package net.neoremind.fountain.packet;

import net.neoremind.fountain.exception.ParamErrorException;
import net.neoremind.fountain.util.ProtocolHelper;

/**
 * @author hanxu
 */
public class OKPacket extends MysqlPacket {

    private static final long serialVersionUID = 939916149041573794L;

    public byte okflag;
    public byte[] affectRows;
    public byte[] insertId;
    public int serverStatus;
    public int warningStatus;
    public String message;

    @Override
    public void fromBytes(byte[] data) {
        if (data == null || data.length == 0) {
            throw new ParamErrorException("data is not valid");
        }

        Position position = new Position();

        // 1 0x00(1)
        okflag = data[position.getPosition()];
        position.increase();

        // 2 affect_rows(N)
        affectRows = ProtocolHelper.getLengthCodedBytes(data, position);

        // 3 insert_id(N)
        insertId = ProtocolHelper.getLengthCodedBytes(data, position);

        // 4 server status(2)
        serverStatus = (int) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 2);

        // 5 waring status(2)
        warningStatus = (int) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 2);

        // 6 message(N)
        message = new String(ProtocolHelper.getFixedBytes(data, position, data.length - position.getPosition()));
    }

    public byte getOkflag() {
        return okflag;
    }

    public void setOkflag(byte okflag) {
        this.okflag = okflag;
    }

    public byte[] getAffectRows() {
        return affectRows;
    }

    public void setAffectRows(byte[] affectRows) {
        this.affectRows = affectRows;
    }

    public byte[] getInsertId() {
        return insertId;
    }

    public void setInsertId(byte[] insertId) {
        this.insertId = insertId;
    }

    public int getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(int serverStatus) {
        this.serverStatus = serverStatus;
    }

    public int getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(int warningStatus) {
        this.warningStatus = warningStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

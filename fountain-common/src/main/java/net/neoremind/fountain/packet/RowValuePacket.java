package net.neoremind.fountain.packet;

import java.util.ArrayList;
import java.util.List;

import net.neoremind.fountain.util.ProtocolHelper;

/**
 * 描述Text Protocol 协议的row 数据包
 *
 * @author zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/com-query-response
 * .html#packet-ProtocolText::ResultsetRow">ProtocolText::ResultsetRow</a>
 * @since 2013年8月3日
 */
public class RowValuePacket extends MysqlPacket {

    private static final long serialVersionUID = -7343791353841856383L;

    private List<String> fieldValueList = new ArrayList<String>();

    @Override
    public void fromBytes(byte[] data) {
        Position position = new Position();

        while (position.getPosition() < data.length) {
            String value = null;

            int dataLength = (int) ProtocolHelper.getLengthCodedLength(data, position);
            if (dataLength != ProtocolHelper.NULL_LENGTH) {
                value = new String(ProtocolHelper.getFixedBytes(data, position, dataLength));
            }
            fieldValueList.add(value);
        }
    }

    public List<String> getFieldValueList() {
        return fieldValueList;
    }

    public void setFieldValueList(List<String> fieldValueList) {
        this.fieldValueList = fieldValueList;
    }

}

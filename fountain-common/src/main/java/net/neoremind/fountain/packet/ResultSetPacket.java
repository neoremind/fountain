package net.neoremind.fountain.packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述Text Protocol返回结果数据包
 *
 * @author hanxu
 */
public class ResultSetPacket extends MysqlPacket {

    private static final long serialVersionUID = -293398433691579613L;

    private List<FieldDescriptionPacket> fieldDescriptionList = new ArrayList<FieldDescriptionPacket>();
    private List<RowValuePacket> rowValueList = new ArrayList<RowValuePacket>();

    public List<FieldDescriptionPacket> getFieldDescriptionList() {
        return fieldDescriptionList;
    }

    public void setFieldDescriptionList(List<FieldDescriptionPacket> fieldDescriptionList) {
        this.fieldDescriptionList = fieldDescriptionList;
    }

    public List<RowValuePacket> getRowValueList() {
        return rowValueList;
    }

    public void setRowValueList(List<RowValuePacket> rowValueList) {
        this.rowValueList = rowValueList;
    }

    @Override
    public void fromBytes(byte[] data) {
        // do nothing
    }

}

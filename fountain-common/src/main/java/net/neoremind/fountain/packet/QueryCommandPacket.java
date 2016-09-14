package net.neoremind.fountain.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.neoremind.fountain.util.CommandTypeConstant;

/**
 * 查询指令数据包，继承自{@link CommandPacket}
 *
 * @author zhangxu
 */
public class QueryCommandPacket extends CommandPacket {

    private static final long serialVersionUID = -4938658438038905663L;

    /**
     * 构造方法，无参数
     */
    public QueryCommandPacket() {
        super.setCommand(CommandTypeConstant.COM_QUERY);
    }

    /**
     * 转换成bytes
     *
     * @return bytes
     *
     * @throws IOException IOException
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 1. command(1)
        out.write(super.getCommand());

        // 2. sql(n)
        out.write(super.getSql().getBytes());

        return out.toByteArray();
    }

    @Override
    public void fromBytes(byte[] data) {
        // do nothing
    }
}

package net.neoremind.fountain.producer.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import net.neoremind.fountain.packet.CommandPacket;
import net.neoremind.fountain.packet.EOFPacket;
import net.neoremind.fountain.util.CommandTypeConstant;
import net.neoremind.fountain.util.ProtocolHelper;

/**
 * 传统的dump binlog指令包
 *
 * @author hanxu03, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/com-binlog-dump.html">com-binlog-dump</a>
 * @since 2013-7-15
 */
public class BinLogDumpCommandPacket extends CommandPacket {

    private static final long serialVersionUID = 3422108675874199118L;

    /**
     * position in the binlog-file to start the stream with
     */
    private int binlogPos;

    /**
     * 默认是0x00，没有event时阻塞客户端接受线程。<br/>
     * 可以选择0x01表示使用BINLOG_DUMP_NON_BLOCK模式，当没有event时，不阻塞，而是发送{@link EOFPacket}，
     */
    private final int flags = 0;

    /**
     * server id of this slave <br/>
     * 每个slave从库必须不同，否则主库不会接受这个slave
     */
    private int serverId;

    /**
     * filename of the binlog on the master
     */
    private String binlogFileName;

    /**
     * 构造函数，无参数
     */
    public BinLogDumpCommandPacket() {
        setCommand(CommandTypeConstant.COM_BINLOG_DUMP);
    }

    public int getBinlogPos() {
        return binlogPos;
    }

    public void setBinlogPos(int binlogPos) {
        this.binlogPos = binlogPos;
    }

    public int getFlags() {
        return flags;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getBinlogFileName() {
        return binlogFileName;
    }

    public void setBinlogFileName(String binlogFileName) {
        this.binlogFileName = binlogFileName;
    }

    /**
     * 转换为bytes
     *
     * @return bytes
     *
     * @throws IOException IOException
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 1 [12] COM_BINLOG_DUMP 4 binlog-pos 2 flags 4 server-id string[EOF]
        // binlog-filename
        // 1. COM_BINLOG_DUMP(1)
        out.write(CommandTypeConstant.COM_BINLOG_DUMP);

        // 2. binlog-pos(4)
        ProtocolHelper.writeIntWithByteByLittleEndian(binlogPos, 4, out);

        // 3. flags(2)
        ProtocolHelper.writeIntWithByteByLittleEndian(flags, 2, out);

        // 4. server-id(4)
        ProtocolHelper.writeIntWithByteByLittleEndian(serverId, 4, out);

        // 5. binlog-filename(EOF)
        if (StringUtils.isNotEmpty(this.binlogFileName)) {
            out.write(this.binlogFileName.getBytes());
        }

        return out.toByteArray();
    }

    @Override
    public void fromBytes(byte[] data) {
        // do nothing
    }

}

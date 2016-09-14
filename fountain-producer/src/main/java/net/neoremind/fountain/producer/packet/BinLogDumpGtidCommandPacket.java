package net.neoremind.fountain.producer.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.neoremind.fountain.eventposition.GtIdSet;
import net.neoremind.fountain.packet.CommandPacket;
import net.neoremind.fountain.packet.EOFPacket;
import net.neoremind.fountain.util.CommandTypeConstant;
import net.neoremind.fountain.util.ProtocolHelper;

/**
 * MySQL 5.6版本的根据GTID进行dump binlog的指令包
 * <p/>
 * 根据如下协议格式构造dump packet
 * <pre>
 * 1              [1e] COM_BINLOG_DUMP_GTID
 * 2              flags
 * 4              server-id
 * 4              binlog-filename-len
 * string[len]    binlog-filename
 * 8              binlog-pos
 * if flags & BINLOG_THROUGH_GTID {
 * 4              data-size
 * string[len]    data
 * }
 * </pre>
 *
 * @author zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/com-binlog-dump-gtid.html">com-binlog-dump-gtid</a>
 * @since 2013-7-15
 */
public class BinLogDumpGtidCommandPacket extends CommandPacket {

    private static final long serialVersionUID = 3422108675874199118L;

    /**
     * 默认是0x00，没有event时阻塞客户端接受线程。<br/>
     * 可以选择0x01表示使用BINLOG_DUMP_NON_BLOCK模式，当没有event时，不阻塞，而是发送{@link EOFPacket}。
     * 向master发送COM_BINLOG_DUMP_GTID命令。在发送dump命令的时候，我们可以指定flag为BINLOG_DUMP_NON_BLOCK，这样master在没有可发送的binlog
     * event之后，就会返回一个EOF package。不过通常对于slave来说，一直把连接挂着可能更好，这样能更及时收到新产生的binlog event。
     */
    private final byte flags = 0x00;

    /**
     * server id of this slave <br/>
     * 每个slave从库必须不同，否则主库不会接受这个slave
     */
    private int serverId = 10;

    /**
     * gtid集合
     *
     * @see GtIdSet
     */
    private GtIdSet gtIdSet;

    /**
     * 构造函数，无参数
     */
    public BinLogDumpGtidCommandPacket() {
        setCommand(CommandTypeConstant.COM_BINLOG_DUMP_GTID);
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

        // 1. [1e] COM_BINLOG_DUMP_GTID(1)
        out.write(CommandTypeConstant.COM_BINLOG_DUMP_GTID);

        // 2. flags(2)
        ProtocolHelper.writeIntWithByteByLittleEndian(flags, 2, out);

        // 3. server-id(4)
        ProtocolHelper.writeIntWithByteByLittleEndian(serverId, 4, out);

        // 4. binlog-filename-len(4)
        ProtocolHelper.writeIntWithByteByLittleEndian(3, 4, out);

        // 5. binlog-filename(string[len])
        out.write(0x00);
        out.write(0x00);
        out.write(0x00);

        // 6.  binlog-pos(8)
        ProtocolHelper.writeUnsignedLongByLittleEndian(4, out);

        // 7. data-size(4)
        ProtocolHelper.writeUnsignedIntByLittleEndian(gtIdSet.length(), out);

        // 8. data(string[len])
        gtIdSet.encode(out);

        return out.toByteArray();
    }

    @Override
    public void fromBytes(byte[] data) {
        // do nothing
    }

    public GtIdSet getGtIdSet() {
        return gtIdSet;
    }

    public void setGtIdSet(GtIdSet gtIdSet) {
        this.gtIdSet = gtIdSet;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

}

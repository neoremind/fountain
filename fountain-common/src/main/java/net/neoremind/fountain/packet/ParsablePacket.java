package net.neoremind.fountain.packet;

/**
 * 可解析的mysql数据包
 *
 * @author hexiufeng
 */
public interface ParsablePacket {

    /**
     * 解析bytes数据包
     *
     * @param data mysql数据包
     */
    void fromBytes(byte[] data);
}

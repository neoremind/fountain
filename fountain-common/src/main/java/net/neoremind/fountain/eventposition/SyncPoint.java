package net.neoremind.fountain.eventposition;

/**
 * 描述mysql的数据同步点，用来代替EventPosition.
 * <p>
 * mysql的同步点可能是baidu groupid, binlogfilename and position,或者mysql gt id for mysql 5.6.9+
 * </p>
 *
 * @author hexiufeng
 */
public interface SyncPoint {
    /**
     * convert 同步点对象为byte数组
     *
     * @return byte array
     */
    byte[] toBytes();

    /**
     * 转换数组为同步点对象
     *
     * @param buf 同步点数组
     */
    void parse(byte[] buf);
}

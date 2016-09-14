package net.neoremind.fountain.rowbaselog.event;

/**
 * 用于保存TableMapEvent数据的回调
 *
 * @author hexiufeng
 */
public interface TableEventCallback {
    /**
     * 保存TableMapEvent事件
     *
     * @param tableMapEvent TableMapEvent 事件
     */
    void acceptTableMapEvent(TableMapEvent tableMapEvent);
}

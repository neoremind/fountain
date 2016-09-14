package net.neoremind.fountain.rowbaselog.event;

/**
 * {@link FormatDescriptionEvent}中处理接下来event的头length
 *
 * @author hexiufeng, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/format-description-event.html">format-description-event</a>
 */
public interface FormatInfo {

    /**
     * 剩余的header length，一般是19，但是对于百度的MySQL，会是27，加入了8个byte的groupid
     *
     * @return 剩余头字节数组长度
     */
    int getExtraHeadersLength();

    /**
     * post header 长度
     *
     * @param eventType 事件类型
     *
     * @return 字节数组长度
     */
    int getPostHeaderLen(int eventType);

}

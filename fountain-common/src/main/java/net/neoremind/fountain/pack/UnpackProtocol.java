package net.neoremind.fountain.pack;

/**
 * 把byte[]解析成java对象的抽象描述
 *
 * @author hexiufeng
 */
public interface UnpackProtocol {
    /**
     * 解包方法
     *
     * @param event 需要解包的对象，可能是json string
     *
     * @return 解包后的java对象
     */
    <T> T unpack(Object event);
}

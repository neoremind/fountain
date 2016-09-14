package net.neoremind.fountain.pack;

/**
 * 把一个java对象根据相应的协议打包，可能打包为bin，或者json，或者其他的\t分割文件
 *
 * @author heixufeng
 */
public interface PackProtocol {
    /**
     * 将event对象打包成可以下发的对象，可能是json string，byte[]或其他方式
     *
     * @param event 事件对象
     *
     * @return 打包后的对象
     */
    Object pack(Object event);
}

package net.neoremind.fountain.pack;

/**
 * 解包数据时，把byte[]转化为java对象时需要提供类名称，有些类 可能是复杂的泛型，需要构造出其class，本接口就是构造class对象的 抽象描述
 *
 * @author hexiufeng
 */
public interface UnpackClazResolver {
    /**
     * 需要指定的class类型
     *
     * @return {@link Class} 对象
     */
    Class<?> getClaz();
}

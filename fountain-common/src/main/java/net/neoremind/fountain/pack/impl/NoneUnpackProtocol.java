package net.neoremind.fountain.pack.impl;

import net.neoremind.fountain.pack.UnpackProtocol;

/**
 * 不做任何处理直接返回
 *
 * @author hexiufeng
 */
public class NoneUnpackProtocol implements UnpackProtocol {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unpack(Object event) {
        return (T) event;
    }

}

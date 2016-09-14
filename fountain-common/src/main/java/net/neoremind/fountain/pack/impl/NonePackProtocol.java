package net.neoremind.fountain.pack.impl;

import net.neoremind.fountain.pack.PackProtocol;

/**
 * 不做任何处理直接返回，用于producer和consumer在同一jvm内的场景
 *
 * @author hexiufeng
 */
public class NonePackProtocol implements PackProtocol {

    @Override
    public Object pack(Object event) {
        return event;
    }

}

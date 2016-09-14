package net.neoremind.fountain.pack.impl;

import net.neoremind.fountain.pack.PackProtocol;
import net.neoremind.fountain.util.JsonUtils;

/**
 * 使用json进行打包
 *
 * @author hexiufeng
 */
public class JsonPackProtocol implements PackProtocol {
    @Override
    public Object pack(Object event) {
        String json = JsonUtils.toJson(event);
        if (json == null) {
            throw new RuntimeException("convert error.");
        }
        return json;
    }
}

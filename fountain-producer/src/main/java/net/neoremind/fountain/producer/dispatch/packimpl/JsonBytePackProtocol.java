package net.neoremind.fountain.producer.dispatch.packimpl;

import net.neoremind.fountain.pack.PackProtocol;
import net.neoremind.fountain.util.JsonUtils;

/**
 * 使用json smile进行打包，输出byte[]
 *
 * @author hexiufeng
 */
public class JsonBytePackProtocol implements PackProtocol {
    @Override
    public Object pack(Object event) {
        byte[] jsonBytes = JsonUtils.toJsonBytes(event);
        if (jsonBytes == null) {
            throw new RuntimeException("convert error.");
        }
        return jsonBytes;
    }

}

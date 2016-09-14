package net.neoremind.fountain.pack.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.pack.UnpackClazResolver;
import net.neoremind.fountain.pack.UnpackProtocol;
import net.neoremind.fountain.util.JsonUtils;

/**
 * 使用json协议进行解包
 *
 * @author hexiufeng
 */
public class JsonUnpackProtocol implements UnpackProtocol {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUnpackProtocol.class);

    private UnpackClazResolver unpackClazResolver;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unpack(Object event) {
        if (event instanceof String) {
            LOGGER.debug((String) event);
            return (T) JsonUtils.json2Object((String) event, unpackClazResolver.getClaz());
        }
        throw new InvalidUnpackType();
    }

    public UnpackClazResolver getUnpackClazResolver() {
        return unpackClazResolver;
    }

    public void setUnpackClazResolver(UnpackClazResolver unpackClazResolver) {
        this.unpackClazResolver = unpackClazResolver;
    }
}

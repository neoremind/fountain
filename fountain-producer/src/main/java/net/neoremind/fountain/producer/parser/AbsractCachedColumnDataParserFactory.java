package net.neoremind.fountain.producer.parser;

import java.util.HashMap;
import java.util.Map;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.ColumnDataParserFactory;
import net.neoremind.fountain.meta.ColumnTypeEnum;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

/**
 * {@link ColumnDataParserFactory
 * ColumnDataParserFactory}的抽象实现。 支持databus和row base
 * binlong协议，内部会缓存每一种类型的ColumnDataParser实例。
 * 
 * @author hexiufeng
 * 
 */
@UnThreadSafe
public abstract class AbsractCachedColumnDataParserFactory implements
        ColumnDataParserFactory {
    private Map<ColumnTypeEnum, ColumnDataParser> parserMap =
            new HashMap<ColumnTypeEnum, ColumnDataParser>();
    private boolean inited = false;

    @Override
    public ColumnDataParser factory(ColumnTypeEnum typeEnum) {
        if (!inited) {
            initFactory();
            inited = true;
        }
        return parserMap.get(typeEnum);
    }

    private void initFactory() {
        for (ColumnTypeEnum typeEnum : ColumnTypeEnum.values()) {
            parserMap.put(typeEnum, createColumnDataParser(typeEnum));
        }
    }

    /**
     * 根据ColumnTypeEnum构建ColumnDataParser实例，针对databus和row base binlong协议有所区别
     * 
     * @param typeEnum
     * @return
     */
    protected abstract ColumnDataParser createColumnDataParser(
            ColumnTypeEnum typeEnum);
}

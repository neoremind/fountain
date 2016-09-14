package net.neoremind.fountain.event;

import net.neoremind.fountain.meta.ColumnTypeEnum;

/**
 * 根据每列的类型生成该类型的{@link ColumnDataParser ColumnDataParser}实例
 *
 * @author hexiufeng
 */
public interface ColumnDataParserFactory {
    /**
     * 根据列的类型生成{@link ColumnDataParser ColumnDataParser}实例
     *
     * @param typeEnum
     *
     * @return
     */
    ColumnDataParser factory(ColumnTypeEnum typeEnum);
}

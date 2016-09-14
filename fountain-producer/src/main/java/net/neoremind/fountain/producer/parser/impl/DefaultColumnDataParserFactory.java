package net.neoremind.fountain.producer.parser.impl;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.ColumnDataParserFactory;
import net.neoremind.fountain.meta.ColumnTypeEnum;
import net.neoremind.fountain.producer.parser.AbsractCachedColumnDataParserFactory;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

@UnThreadSafe
public class DefaultColumnDataParserFactory extends
        AbsractCachedColumnDataParserFactory implements ColumnDataParserFactory {
    @Override
    protected ColumnDataParser createColumnDataParser(ColumnTypeEnum typeEnum) {
        ColumnDataParser parser = null;
        switch (typeEnum) {
            case MYSQL_TYPE_DECIMAL:
                parser = new DecimalColumnDataParser();
                break;
            case MYSQL_TYPE_TINY:
                parser = new TinyIntColumnDataParser();
                break;
            case MYSQL_TYPE_SHORT:
                parser = new ShortColumnDataParser();
                break;
            case MYSQL_TYPE_LONG:
                parser = new IntegerColumnDataParser();
                break;
            case MYSQL_TYPE_FLOAT:
                parser = new FloatColumnDataParser();
                break;
            case MYSQL_TYPE_DOUBLE:
                parser = new DoubleColumnDataParser();
                break;
            case MYSQL_TYPE_NULL:
                break;
            case MYSQL_TYPE_TIMESTAMP:
                parser = new TimeStampColumnDataParser();
                break;
            case MYSQL_TYPE_LONGLONG:
                parser = new LongColumnDataParser();
                break;
            case MYSQL_TYPE_INT24:
                parser = new Int24ColumnDataParser();
                break;
            case MYSQL_TYPE_DATE:
                parser = new DateColumnDataParser();
                break;
            case MYSQL_TYPE_TIME:
                parser = new TimeColumnDataParser();
                break;
            case MYSQL_TYPE_DATETIME:
                parser = new DateTimeColumnDataParser();
                break;
            case MYSQL_TYPE_YEAR:
                parser = new YearColumnDataParser();
                break;
            case MYSQL_TYPE_NEWDATE:
                parser = new DateColumnDataParser();
                break;
            case MYSQL_TYPE_VARCHAR:
                parser = new VarcharStringColumnDataParser();
                break;
            case MYSQL_TYPE_BIT:
                break;
            case MYSQL_TYPE_TIMESTAMP2:
                parser = new TimeStamp2ColumnDataParser();
                break;
            case MYSQL_TYPE_DATETIME2:
                parser = new DateTime2ColumnDataParser();
                break;
            case MYSQL_TYPE_TIME2:
                parser = new Time2ColumnDataParser();
                break;
            case MYSQL_TYPE_NEWDECIMAL:
                parser = new DecimalColumnDataParser();
                break;
            case MYSQL_TYPE_ENUM:
                parser = new EnumColumnDataParser();
                break;
            case MYSQL_TYPE_SET:
                parser = new BitColumnDataParser();
                break;
            case MYSQL_TYPE_TINY_BLOB:
                parser = new BlobColumnDataParser();
                break;
            case MYSQL_TYPE_MEDIUM_BLOB:
                parser = new BlobColumnDataParser();
                break;
            case MYSQL_TYPE_LONG_BLOB:
                parser = new BlobColumnDataParser();
                break;
            case MYSQL_TYPE_BLOB:
                parser = new BlobColumnDataParser();
                break;
            case MYSQL_TYPE_VAR_STRING:
                parser = new VarcharStringColumnDataParser();
                break;
            case MYSQL_TYPE_STRING:
                parser = new FixeStringColumnDataParser();
                break;
            case MYSQL_TYPE_GEOMETRY:
                break;
            default:
                throw new RuntimeException("Invalid column type.");
        }
        return parser;
    }

}

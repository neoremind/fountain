package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;

/**
 * 特别用在MySQL5.6中，默认datetime，在官方文档中对应datetime2类型，需要用这个parser解析。
 * 原因在于，在一般情况下有助于进一步压缩数据、节约存储空间，老的{@link DateTimeColumnDataParser}需要8byte，而新的datetime2存储规则为：
 * 5个byte，按照位切分来存储year、month、day、hour、min、sec、microsec。
 *
 * @author zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/binary-protocol-value
 * .html#packet-ProtocolBinary::MYSQL_TYPE_DATETIME">MYSQL_TYPE_DATETIME</a>
 * @since 2015-12-06
 */
public class DateTime2ColumnDataParser implements ColumnDataParser {

    public static final long DATETIMEF_INT_OFS = 0x8000000000L;

    @Override
    public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
        Calendar cal = Calendar.getInstance();
        byte[] array = new byte[5];
        buf.get(array);
        long time = getBigEndianLongOf40bit(array) - DATETIMEF_INT_OFS;
        long ymd = time >> 17;
        long ym = ymd >> 5;
        long hms = time % (1 << 17);

        int year = (int) (ym / 13);
        int month = (int) (ym % 13) - 1;
        int day = (int) (ymd % (1 << 5));
        int hour = (int) (hms >> 12);
        int minute = (int) ((hms >> 6) % (1 << 6));
        int second = (int) (hms % (1 << 6));

        cal.set(year, month, day, hour, minute, second);

        columnData.setValue(new Timestamp(cal.getTimeInMillis()));
        columnData.setJavaType(Types.TIMESTAMP);
        columnData.setSqlType(meta.getTypeEnum().getTypeValue());
    }

    /**
     * 按照大尾端还原一个40bit的long
     *
     * @see mysql-5.6.10/include/myisampack.h - mi_uint5korr
     */
    public final long getBigEndianLongOf40bit(byte[] buf) {
        int position = 0;
        return ((long) (0xff & buf[position++]) << 32) | ((long) (0xff & buf[position++]) << 24)
                | ((long) (0xff & buf[position++]) << 16) | ((long) (0xff & buf[position++]) << 8)
                | ((long) (0xff & buf[position++]));
    }

}

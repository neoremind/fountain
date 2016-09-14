package net.neoremind.fountain.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.BitSet;

import net.neoremind.fountain.exception.DataErrorException;
import net.neoremind.fountain.packet.PacketHeader;
import net.neoremind.fountain.packet.Position;

/**
 * mysql binlog协议解析工具
 * 
 * @author hanxu03
 * 
 *         2013-7-9
 */
public class ProtocolHelper {

    private static final int NULL_TERMINATED_STRING_DELIMITER = 0;

    public static final int NULL_LENGTH = -1;

    public static final int TINYINT_8_MAX_VALUE = 256;

    public static final int SMALLINT_16_MAX_VALUE = 65536;

    public static final int MEDIUMINT_24_MAX_VALUE = 16777216;

    public static final long INTEGER_32_MAX_VALUE = 4294967296L;

    public static final BigInteger BIGINT_64_MAX_VALUE = new BigInteger("18446744073709551616");

    /**
     * 解析协议头
     * 
     * @param data bytes数据包
     * @return PacketHeader
     */
    public static PacketHeader getProtocolHeader(byte[] data) {
        if (data == null || data.length != 4) {
            return null;
        }
        return new PacketHeader((int) getUnsignedIntByLittleEndian(data, new Position(), 3), data[3]);
    }

    /**
     * 读取以null结束的数据
     * 
     * @param data 指定数据包
     * @param position 开始位置
     * @return bytes
     */
    public static byte[] getNullTerminatedByte(byte[] data, Position position) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        int len = data.length;
        for (int i = position.getPosition(); i < len; i++) {
            byte b = data[i];
            if (b == NULL_TERMINATED_STRING_DELIMITER) {
                break;
            }
            byteStream.write(b);
        }

        byte[] result = byteStream.toByteArray();
        position.increase(result.length + 1);
        return result;
    }

    /**
     * 获取以null结尾的string
     *
     * @param data 指定数据包
     * @param position 开始位置
     * @return string
     */
    public static String getNullTerminatedString(byte[] data, Position position) {
        return new String(getNullTerminatedByte(data, position));
    }

    /**
     * 输出以null结尾的string
     *
     * @param str string数据
     * @param out 输出buffer
     * @throws IOException io异常
     */
    public static void writeNullTerminatedString(String str, ByteArrayOutputStream out) throws IOException {
        out.write(str.getBytes());
        out.write(MysqlCommonConstants.NULL_TERMINATED_STRING_DELIMITER);
    }

    /**
     * 读取小端int
     *
     * @param data 数据包
     * @param position 开始位置
     * @param length 读取长度，<=4
     * @return int
     */
    public static int getIntByLittleEndian(byte[] data, Position position, int length) {
        return (int) getLongByLittleEndian(data, position, length);
    }

    /**
     * 读取小端long
     *
     * @param data 数据包
     * @param position 开始位置
     * @param length 读取长度，<=8
     * @return long
     */
    public static long getLongByLittleEndian(byte[] data, Position position, int length) {

        long number = 0;
        int offsetRadio = 0;
        int beginPos = position.getPosition();

        for (int i = beginPos; i < beginPos + length - 1; i++) {
            long tmp = 0xff & data[i];
            if (offsetRadio > 0) {
                tmp = tmp << (offsetRadio * 8);
            }

            number = number | tmp;
            offsetRadio++;
        }

        long tmp = 0xff & data[beginPos + length - 1];
        number = number | (tmp << ((length - 1) * 8));

        position.increase(length);
        return number;
    }

    /**
     * 读取无符号int，使用long类型接收
     *
     * @param data 数据包
     * @param position 开始位置
     * @param length 长度，<=4
     * @return long
     */
    public static long getUnsignedIntByLittleEndian(byte[] data, Position position, int length) {
        long number = 0;
        int offsetRadio = 0;
        int beginPos = position.getPosition();

        for (int i = beginPos; i < beginPos + length; i++) {
            long tmp = 0xff & data[i];
            if (offsetRadio > 0) {
                tmp = tmp << (offsetRadio * 8);
            }

            number = number | tmp;
            offsetRadio++;
        }

        position.increase(length);
        return number;
    }

    /**
     * 读取无符号long，使用BigInteger接收
     *
     * @param data 数据包
     * @param position 开始位置
     * @param length 长度，=8
     * @return BigInteger
     */
    public static BigInteger getUnsignedLongByLittleEndian(byte[] data, Position position, int length) {
        final long long64 = getLongByLittleEndian(data, position, length);

        return (long64 >= 0) ? BigInteger.valueOf(long64) : BIGINT_64_MAX_VALUE.add(BigInteger.valueOf(long64));
    }

    /**
     * 输出小端无符号int
     *
     * @param data int 数据
     * @param out ByteArrayOutputStream
     */
    public static void writeUnsignedIntByLittleEndian(int data, ByteArrayOutputStream out) {
        out.write((byte) (data & 0xFF));
        out.write((byte) (data >>> 8));
        out.write((byte) (data >>> 16));
        out.write((byte) (data >>> 24));
    }

    /**
     * 输出小端无符号int
     *
     * @param data int 数据
     * @param out ByteArrayOutputStream
     */
    public static void writeUnsignedLongByLittleEndian(long data, ByteArrayOutputStream out) {
        out.write((byte) (data & 0xFF));
        out.write((byte) (data >>> 8));
        out.write((byte) (data >>> 16));
        out.write((byte) (data >>> 24));
        out.write((byte) (data >>> 32));
        out.write((byte) (data >>> 40));
        out.write((byte) (data >>> 48));
        out.write((byte) (data >>> 56));
    }

    /**
     * 输出指定位数int
     *
     * @param data int 数据
     * @param byteCnt 指定位数
     * @param out ByteArrayOutputStream
     */
    public static void writeIntWithByteByLittleEndian(int data, int byteCnt, ByteArrayOutputStream out) {
        if (byteCnt >= 1) {
            out.write((byte) (data & 0xFF));
        }
        if (byteCnt >= 2) {
            out.write((byte) (data >>> 8));
        }
        if (byteCnt >= 3) {
            out.write((byte) (data >>> 16));
        }
        if (byteCnt >= 4) {
            out.write((byte) (data >>> 24));
        }
    }

    /**
     * 获取float，小端
     *
     * @param data 数据包
     * @param position 开始位置
     * @return float
     */
    public static float getFloatByLittleEndian(byte[] data, Position position) {
        int cnt = getIntByLittleEndian(data, position, 4);
        return Float.intBitsToFloat(cnt);
    }

    /**
     * 获取double，小端
     *
     * @param data 数据包
     * @param position 开始位置
     * @return double
     */
    public static double getDoubleByLittleEndian(byte[] data, Position position) {
        long cnt = getLongByLittleEndian(data, position, 8);
        return Double.doubleToLongBits(cnt);
    }

    /**
     * 获取指定的固定长度字节
     *
     * @param data 数据包
     * @param position 开始位置
     * @param length 指定长度
     * @return bytes
     */
    public static byte[] getFixedBytes(byte[] data, Position position, int length) {
        byte[] result = new byte[length];
        System.arraycopy(data, position.getPosition(), result, 0, length);
        position.increase(length);
        return result;
    }

    /**
     * 输出length code bytes
     *
     * @param data 指定长度
     * @param out ByteArrayOutputStream
     * @throws IOException IOException
     */
    public static void writeLengthCodedBinary(byte[] data, ByteArrayOutputStream out) throws IOException {
        if (data.length < 251) {
            out.write(data.length);
        } else if (data.length < (1 << 16)) {
            out.write(252);
            writeIntWithByteByLittleEndian(data.length, 2, out);
        } else if (data.length < (1 << 24)) {
            out.write(253);
            writeIntWithByteByLittleEndian(data.length, 3, out);
        } else {
            out.write(254);
            writeIntWithByteByLittleEndian(data.length, 4, out);
        }

        out.write(data);
        out.toByteArray();
    }

    /**
     * 获取lengthCode bytes
     *
     * @param data 数据包
     * @param position 开始位置
     * @return bytes
     */
    public static byte[] getLengthCodedBytes(byte[] data, Position position) {
        byte firstByte = (byte) getUnsignedIntByLittleEndian(data, position, 1);
        if (firstByte == 251) {
            return new byte[] {};
        }

        if (firstByte < 251) {
            return new byte[] { firstByte };
        }

        switch (firstByte) {
            case (byte) 252: {
                return getFixedBytes(data, position, 2);
            }
            case (byte) 253: {
                return getFixedBytes(data, position, 3);
            }
            case (byte) 254: {
                byte[] b = getFixedBytes(data, position, 4);
                position.increase(4);
                return b;
            }
            default: {
                throw new DataErrorException("length coded length do not valid");
            }
        }
    }

    /**
     * 获取coded length
     *
     * @param data 数据包
     * @param position 开始位置
     * @return long
     */
    public static long getLengthCodedLength(byte[] data, Position position) {

        int firstByte = (int) getUnsignedIntByLittleEndian(data, position, 1);
        if (firstByte == 251) {
            return NULL_LENGTH;
        }

        if (firstByte < 251) {
            return firstByte;
        }

        switch (firstByte) {
            case 252: {
                return getUnsignedIntByLittleEndian(data, position, 2);
            }
            case 253: {
                return getUnsignedIntByLittleEndian(data, position, 3);
            }
            case 254: {
                long result = getUnsignedIntByLittleEndian(data, position, 4);
                position.increase(4);
                return result;
            }
            default: {
                throw new DataErrorException("length coded length do not valid");
            }
        }
    }

    /**
     * 解析位图
     *
     * @param data 数据包
     * @param position 开始位置
     * @param columnsLength 列数
     * @param bitSet 位图
     */
    public static void fillBitMap(byte[] data, Position position, int columnsLength, BitSet bitSet) {
        for (int index = 0; index < columnsLength; index += 8) {

            byte tmpByte = getFixedBytes(data, position, 1)[0];

            if ((tmpByte & 0x01) != 0) {
                bitSet.set(index);
            }
            if ((tmpByte & 0x02) != 0) {
                bitSet.set(index + 1);
            }
            if ((tmpByte & 0x04) != 0) {
                bitSet.set(index + 2);
            }
            if ((tmpByte & 0x08) != 0) {
                bitSet.set(index + 3);
            }
            if ((tmpByte & 0x10) != 0) {
                bitSet.set(index + 4);
            }
            if ((tmpByte & 0x20) != 0) {
                bitSet.set(index + 5);
            }
            if ((tmpByte & 0x40) != 0) {
                bitSet.set(index + 6);
            }
            if ((tmpByte & 0x80) != 0) {
                bitSet.set(index + 7);
            }
        }
    }

    /**
     * 转换byte to ansi string
     *
     * @param buffer 数据包
     * @param startPos 开始位置
     * @param length 长度
     * @return ansi string
     */
    public static final String toAsciiString(byte[] buffer, int startPos, int length) {
        char[] charArray = new char[length];
        int readpoint = startPos;

        for (int i = 0; i < length; i++) {
            charArray[i] = (char) buffer[readpoint];
            readpoint++;
        }

        return new String(charArray);
    }

    /**
     * 转换byte to ansi string
     *
     * @param buffer 数据包
     * @return ansi string
     */
    public static final String toAsciiString(byte[] buffer) {
        return toAsciiString(buffer, 0, buffer.length);
    }

}

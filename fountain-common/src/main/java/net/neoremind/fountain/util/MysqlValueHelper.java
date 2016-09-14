package net.neoremind.fountain.util;

import java.nio.ByteBuffer;

import net.neoremind.fountain.exception.LengthEncodedIntegerEOFExp;
import net.neoremind.fountain.exception.LengthEncodedIntegerErrExp;
import net.neoremind.fountain.exception.LengthEncodedIntegerNullExp;

public class MysqlValueHelper {
    private static final int NULL_TERMINATED_STRING_DELIMITER = 0;

    private MysqlValueHelper() {
    };

    public static byte[] getFixedBytes(ByteBuffer buffer, int length) {
        byte[] result = new byte[length];
        buffer.get(result);
        return result;
    }

    public static long convertLengthCodedLength(ByteBuffer buffer) throws LengthEncodedIntegerEOFExp,
            LengthEncodedIntegerNullExp, LengthEncodedIntegerErrExp, RuntimeException {
        int magic = UnsignedNumberHelper.toUnsignedByte(buffer.get());
        if (magic < 0xfb) {
            return magic;
        }
        if (magic == 0xfc) {
            return UnsignedNumberHelper.convertLittleEndianUnsignedInt(buffer, 2);
        }
        if (magic == 0xfd) {
            return UnsignedNumberHelper.convertLittleEndianUnsignedInt(buffer, 3);
        }
        if (magic == 0xfe) {
            if (buffer.remaining() >= 8) {
                return UnsignedNumberHelper.convertLittleEndianLong(buffer, 8);
            }
            throw new LengthEncodedIntegerEOFExp();
        }
        if (magic == 0xfb) {
            throw new LengthEncodedIntegerNullExp();
        }
        if (magic == 0xff) {
            throw new LengthEncodedIntegerErrExp();
        }
        throw new RuntimeException("unknown error.");
    }

    public static byte[] getNullTerminatedByte(ByteBuffer buffer) {
        int pos = buffer.position();
        byte[] array = buffer.array();
        int endPos = array.length - 1;
        for (int i = pos; i < array.length; i++) {
            if (NULL_TERMINATED_STRING_DELIMITER == array[i]) {
                endPos = i;
                break;
            }
        }
        byte[] vBuf = new byte[endPos - pos];
        buffer.get(vBuf);
        buffer.position(buffer.position() + 1);
        return vBuf;
    }
}

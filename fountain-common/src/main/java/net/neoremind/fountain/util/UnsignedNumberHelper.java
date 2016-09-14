package net.neoremind.fountain.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 转换数字为无符号的工具类
 * 
 * @author hexiufeng
 * 
 */
public abstract class UnsignedNumberHelper {
	private static final int INT_SIZE = 4; 
	private static final int LONG_SIZE = 8; 
	private static final String DEFAULT_CHARSET = "utf-8";
	// B符号的4个字节的最大值
	private static final long UNSIGNED_INT_MAX = 0xffffffffL;
	private static final int UNSIGNED_BYTE_MAX = 0xff;

	private static final ThreadLocal<String> charSet = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return DEFAULT_CHARSET;
		}
	};

	/**
	 * 用long数据类型表示无符号的int，把高4个字节置为0
	 * 
	 * @param value
	 * @return
	 */
	public static long toUnsignedInt(long value) {
		return value &= UNSIGNED_INT_MAX;
	}

	public static void configCharset(String charset) {
		charSet.set(charset);
	}

	public static int toUnsignedByte(int value) {
		return value &= UNSIGNED_BYTE_MAX;
	}

	public static BigInteger convertLittleEndianLongByteArray2BigInteger(byte[] buf) {
		for (int i = 0; i < buf.length / 2; i++) {
			byte tmp = buf[i];
			buf[i] = buf[buf.length - i - 1];
			buf[buf.length - i - 1] = tmp;
		}
		return new BigInteger(1, buf);
	}

	public static String convertByteArray2String(byte[] buf) {
		String str = null;

		try {
			str = new String(buf, charSet.get());
		} catch (UnsupportedEncodingException e) {
			// ignore

		}
		return str;
	}
	public static String convertByteArray2String(byte[] buf,String chset) {
        String str = null;

        try {
            str = new String(buf, chset);
        } catch (UnsupportedEncodingException e) {
            // ignore

        }
        return str;
    }
	public static long convertLittleEndianUnsignedInt(ByteBuffer buff,int size){
	    if(size <=0 || size > INT_SIZE){
	        throw new IllegalArgumentException("size MUST be less 4.");
	    }
	    byte[] array = new byte[size];
	    buff.get(array);
	    ByteBuffer bbf = ByteBuffer.allocate(INT_SIZE);
	    bbf.put(array);
	    bbf.order(ByteOrder.LITTLE_ENDIAN);
	    if(size < INT_SIZE){
	        bbf.position(INT_SIZE);
	    }
	    bbf.flip();
	    return bbf.getInt() & UNSIGNED_INT_MAX;
	}

	public static long convertLittleEndianLong(ByteBuffer buff,int size){
        if(size <=0 || size > LONG_SIZE){
            throw new IllegalArgumentException("size MUST be less 8.");
        }
        byte[] array = new byte[size];
        buff.get(array);
        ByteBuffer bbf = ByteBuffer.allocate(LONG_SIZE);
        bbf.put(array);
        bbf.order(ByteOrder.LITTLE_ENDIAN);
        if(size < LONG_SIZE){
            bbf.position(LONG_SIZE);
        }
        bbf.flip();
        return bbf.getLong();
    }

}

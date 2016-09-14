package net.neoremind.fountain.util;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.commons.lang3.ArrayUtils;

import net.neoremind.fountain.exception.EOFException;
import net.neoremind.fountain.exception.HalfDataSocketTimeoutException;
import net.neoremind.fountain.exception.ParamErrorException;

/**
 * 操作socket的帮住类，支持写入读取数据到socket，封装操作socket的内部细节和异常处理
 *
 * @author hexiufeng, zhangxu
 */
public class SocketHelper {
    /**
     * 从socket中读取指定长度的数据，该方法会堵塞调用线程直到指定长度的数据完全被获取
     *
     * @param socket
     * @param len
     *
     * @return
     *
     * @throws IOException
     */
    public static byte[] getBuffer(Socket socket, int len) throws IOException {
        if (socket == null || len <= 0) {
            throw new ParamErrorException("Socket is null or len <= 0");
        }

        byte[] buffer = new byte[len];
        int remainSize = len;
        int pos = 0;
        synchronized(socket) {
            while (remainSize > 0) {
                int cnt = 0;
                try {
                    cnt = socket.getInputStream().read(buffer, pos, remainSize);
                } catch (SocketTimeoutException e) {
                    if (pos > 0) {
                        throw new HalfDataSocketTimeoutException();
                    } else {
                        throw e;
                    }
                }
                if (cnt == -1) {
                    throw new EOFException("Cannot read any bytes from socket stream");
                }
                pos += cnt;
                remainSize -= cnt;
            }
        }

        return buffer;
    }

    /**
     * 写入指定的数据到socket
     *
     * @param socket
     * @param data
     *
     * @throws IOException
     */
    public static void writeByte(Socket socket, byte[] data) throws IOException {
        if (socket == null || ArrayUtils.isEmpty(data)) {
            throw new ParamErrorException("Channel is null or data is empty");
        }

        synchronized(socket) {
            socket.getOutputStream().write(data);
        }
    }

}

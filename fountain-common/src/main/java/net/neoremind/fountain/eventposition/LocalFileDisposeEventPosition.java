package net.neoremind.fountain.eventposition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

/**
 * 采用本地文件来记录gt id的{@link DisposeEventPosition DisposeEventPosition} 接口实现
 *
 * @author hexiufeng
 */
@UnThreadSafe
public class LocalFileDisposeEventPosition extends RegistableDisposeEventPosition implements DisposeEventPosition {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileDisposeEventPosition.class);

    private static final int LINE_MAX_SIZE = 1024;
    protected static final String INFO_SEP = ":";
    // 本地存储路径，需要在spring内配置
    private String rootPath;

    public LocalFileDisposeEventPosition() {

    }

    public LocalFileDisposeEventPosition(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public synchronized SyncPoint loadSyncPoint() {
        byte[] info = readBytesInfo();
        if (info == null) {
            return null;
        }
        SyncPoint point = createSyncPoint();
        point.parse(info);
        return point;
    }

    @Override
    public synchronized void saveSyncPoint(SyncPoint point) {
        if (point == null) {
            return;
        }
        ByteBuffer buf = ByteBuffer.wrap(point.toBytes());
        writeCore(buf);
        LOGGER.info("Save SyncPoint:{}", point);
    }

    /**
     * 构建SyncPoint对象，继承类可以override该方法
     *
     * @return
     */
    protected SyncPoint createSyncPoint() {
        return new BaiduGroupIdSyncPoint();
    }

    /**
     * 返回文件扩展名
     *
     * @return 文件扩展名，可能是null
     */
    protected String getFileExt() {
        return null;
    }

    /**
     * 返回记录日志的对象
     *
     * @return {@link Logger} 对象
     */
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * 组装保存同步点的文件的 全路径名
     *
     * @return 文件的全路径名
     */
    private String getFilePath() {
        String ext = getFileExt();
        if (ext == null) {
            return rootPath + "/" + super.getInstanceName();
        }
        return rootPath + "/" + super.getInstanceName() + "." + ext;
    }

    /**
     * 从文件中读取二进制数据
     *
     * @return 文件的二进制数据
     */
    private byte[] readBytesInfo() {
        String path = getFilePath();
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        FileChannel fc = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            fc = fis.getChannel();

            ByteBuffer buf = ByteBuffer.allocate(LINE_MAX_SIZE);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            int count = fc.read(buf);
            if (count <= 0) {
                return null;
            }
            byte[] readBytes = new byte[count];
            buf.flip();
            buf.get(readBytes);
            return readBytes;
        } catch (FileNotFoundException e) {
            getLogger().error(null, e);
        } catch (IOException e) {
            getLogger().error(null, e);
        } finally {
            if (fc != null) {
                try {
                    fc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 写二级制数据到文件
     *
     * @param buf {@link ByteBuffer}
     */
    private void writeCore(ByteBuffer buf) {
        checkRootPath();
        String path = getFilePath();
        File file = new File(path);

        FileChannel fc = null;
        try {
            FileOutputStream fis = new FileOutputStream(file);
            fc = fis.getChannel();

            buf.order(ByteOrder.LITTLE_ENDIAN);
            while (buf.remaining() > 0) {
                fc.write(buf);
            }
        } catch (FileNotFoundException e) {
            getLogger().error(null, e);
        } catch (IOException e) {
            getLogger().error(null, e);
        } finally {
            if (fc != null) {
                try {
                    fc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 检查同步点文件路径是否存在
     */
    private void checkRootPath() {
        File dir = new File(rootPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException("SyncPoint file parent path don't exist.");
        }
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public static void main(String[] args) {
        LocalFileDisposeEventPosition dp = new LocalFileDisposeEventPosition();
        dp.setRootPath("/Users/baidu/work/fountain/test");
        dp.registerInstance("test00");
        //dp.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(100L)));
        SyncPoint syncPoint = dp.loadSyncPoint();
        System.out.println(new String(syncPoint.toBytes()));
    }

}

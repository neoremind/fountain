package net.neoremind.fountain.consumer.spi.def;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.consumer.spi.Consumer;

/**
 * 性能测试时使用的消费者，只记录日志
 *
 * @author hexiufeng
 */
public class OutFileConsumer extends AbstractConsumeSingleChangeSetConsumer implements Consumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutFileConsumer.class);

    private String rootPath;
    private FileChannel channel;
    private long idSeq = 0;
    private int index = 0;
    private int limit = 100;
    private int maxLines = 1000000;
    private int fileLines = 0;
    private String fileName = "das";

    public long getIdSeq() {
        return idSeq;
    }

    public void setIdSeq(long idSeq) {
        this.idSeq = idSeq;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public int getFileLines() {
        return fileLines;
    }

    public void setFileLines(int fileLines) {
        this.fileLines = fileLines;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void init() {
        ensureFile();
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void ensureFile() {
        try {
            FileOutputStream fs = new FileOutputStream(getFileName());
            channel = fs.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String getFileName() {
        return rootPath + "/" + fileName + index;
    }

    private ByteBuffer getLineBuffer(Object event, long id) {
        ChangeDataSet ds = (ChangeDataSet) event;
        long cost = System.currentTimeMillis() - ds.getSendTime();
        StringBuilder sb = new StringBuilder();
        sb.append(cost);
        sb.append("###");
        sb.append(Thread.currentThread().getName());
        sb.append("###");
        sb.append(event.toString());
        sb.append("###");
        sb.append(id);
        sb.append('\n');
        try {
            return ByteBuffer.wrap(sb.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized long ensureSeq() {
        idSeq++;
        if (idSeq < 0) {
            idSeq = 1;
        }
        return idSeq;
    }

    @Override
    protected synchronized void outputCore(ChangeDataSet event) throws RuntimeException {
        if (channel == null) {
            ensureFile();
        }
        long id = ensureSeq();
        try {
            channel.write(getLineBuffer(event, id));
        } catch (IOException e) {
            LOGGER.error("save error.", e);
            return;
        }
        fileLines++;
        ChangeDataSet ds = (ChangeDataSet) event;
        LOGGER.info(ds.toString());
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}

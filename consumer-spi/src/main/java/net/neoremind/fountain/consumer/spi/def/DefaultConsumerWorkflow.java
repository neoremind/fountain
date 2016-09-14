package net.neoremind.fountain.consumer.spi.def;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.consumer.spi.Consumer;
import net.neoremind.fountain.consumer.spi.ConsumerWorkflow;
import net.neoremind.fountain.consumer.spi.RecievedDataConverter;
import net.neoremind.fountain.pack.UnpackProtocol;
import net.neoremind.fountain.pack.impl.NoneUnpackProtocol;

/**
 * 缺省提供的数据变化消费流程
 *
 * @author hexiufeng
 */
public class DefaultConsumerWorkflow implements ConsumerWorkflow {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConsumerWorkflow.class);

    /**
     * 解包协议，缺省不解包，用于producer和consumer在同一jvm内
     */
    private UnpackProtocol unPackProtocol = new NoneUnpackProtocol();
    /**
     * 具体消费数据，缺省打出日志
     */
    private Consumer consumer = new Consumer() {

        @Override
        public <T> boolean consume(T event) {
            LOGGER.info(event.toString());
            return true;
        }

    };
    private RecievedDataConverter recievedDataConverter = new RecievedDataConverter() {

        @Override
        public Object covert(Object dataObject) {
            return dataObject;
        }

    };
    /**
     * 是否输出效率统计信息，默认不打印
     */
    private boolean outDebug = false;

    public UnpackProtocol getUnPackProtocol() {
        return unPackProtocol;
    }

    public void setUnPackProtocol(UnpackProtocol unPackProtocol) {
        this.unPackProtocol = unPackProtocol;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public boolean isOutDebug() {
        return outDebug;
    }

    public void setOutDebug(boolean outDebug) {
        this.outDebug = outDebug;
    }

    /**
     * 消费统计信息
     *
     * @author hexiufeng
     */
    private static class DebugInfo {
        long start;
        long next;
        long step = 30000;
        long all;
        long allBuf;

        /**
         * 接收一个消息，记录统计信息
         *
         * @param message
         */
        void doOne(Object message) {
            int inByte = 0;
            if (message instanceof byte[]) {
                byte[] buf = (byte[]) message;
                inByte = buf.length;
            }
            allBuf = allBuf + inByte;
            all++;
            if (start == 0) {
                start = System.currentTimeMillis();
                next = start + step;
                return;
            }
            long now = System.currentTimeMillis();
            if (now >= next) {
                long avg = all * 1000 / (now - start);
                long avgBuf = allBuf / (now - start) * 1000;
                LOGGER.info("avg cost:" + avg + ", avg buf:" + avgBuf);
                next = next + step;
            }
        }
    }

    /**
     * 每线程的消费统计信息
     */
    private static final ThreadLocal<DebugInfo> DEBUG_INFO = new ThreadLocal<DebugInfo>() {
        @Override
        protected DebugInfo initialValue() {
            return new DebugInfo();
        }
    };

    @Override
    public boolean doConsume(Object message) {
        Object unPack = unPackProtocol.unpack(recievedDataConverter.covert(message));
        boolean ret = consumer.consume(unPack);
        if (outDebug) {
            DEBUG_INFO.get().doOne(message);
        }
        return ret;
    }

    public RecievedDataConverter getRecievedDataConverter() {
        return recievedDataConverter;
    }

    public void setRecievedDataConverter(RecievedDataConverter recievedDataConverter) {
        this.recievedDataConverter = recievedDataConverter;
    }
}

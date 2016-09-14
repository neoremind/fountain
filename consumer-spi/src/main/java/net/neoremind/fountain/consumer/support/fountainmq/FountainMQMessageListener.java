package net.neoremind.fountain.consumer.support.fountainmq;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.common.mq.FountainMQ;
import net.neoremind.fountain.consumer.spi.ConsumerWorkflow;

/**
 * 基于内存mq的消息队列接收监控器，启动一个线程从FountainMQ读取消息并委托给ConsumerWorkflow处理
 * 
 * @author hexiufeng
 * 
 */
public class FountainMQMessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FountainMQMessageListener.class);

    private final CountDownLatch threadStartWait = new CountDownLatch(1);
    private volatile CountDownLatch destroyWait;
    private volatile boolean shutDowning = false;
    private int threadStartTimeout = 3000;
    private int popTimeout = 2000;
    private long listenTimeout;

    private FountainMQ fmq;
    private ConsumerWorkflow workflow;
    private String listenerName;
    

    public ConsumerWorkflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(ConsumerWorkflow workflow) {
        this.workflow = workflow;
    }

    public long getListenTimeout() {
        return listenTimeout;
    }

    public void setListenTimeout(long listenTimeout) {
        this.listenTimeout = listenTimeout;
    }

    public FountainMQ getFmq() {
        return fmq;
    }

    public void setFmq(FountainMQ fmq) {
        this.fmq = fmq;
    }

    /**
     * 开启消费线程
     */
    public void start() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                threadStartWait.countDown();
                threadHandler();
            }
        });
        String threadName = "FountainMQMessageListener-";
        if (listenerName != null) {
            threadName = threadName + listenerName;
        }
        t.setName(threadName + "-" + t.getId());
        t.start();
        try {
            threadStartWait.await(threadStartTimeout, TimeUnit.MILLISECONDS);
            LOGGER.info("Succeed to start consumer");
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOGGER.error(null, e);
            throw new RuntimeException();
        }
    }

    public void destroy() {
        destroyWait = new CountDownLatch(1);
        shutDowning = true;
        try {
            destroyWait.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消费处理函数
     */
    private void threadHandler() {
        while (true) {
            Object message = fmq.pop(popTimeout);
            if (message != null) {
                // 处理失败则重复处理
                while (!workflow.doConsume(message)) {
                    if (shutDowning) {
                        destroyWait.countDown();
                        return;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
            if (shutDowning) {
                destroyWait.countDown();
                break;
            }
        }
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

    
}

package net.neoremind.fountain.producer.dispatch.fountainmq;

import java.util.Iterator;

import net.neoremind.fountain.changedata.BinlogTraceable;
import net.neoremind.fountain.common.mq.FountainMQ;
import net.neoremind.fountain.producer.dispatch.DispatchException;
import net.neoremind.fountain.producer.dispatch.Transport;
import net.neoremind.fountain.producer.dispatch.misc.MessageSeparationPolicy;
import net.neoremind.fountain.producer.dispatch.misc.NoSeparationPolicy;

/**
 * 使用内存mq进行下发数据的传输器，用于producer和consumer在同一jvm内的场景
 *
 * @author hexiufeng
 */
public class FoutainMQTransport implements Transport {
    private FountainMQ fmq;
    private MessageSeparationPolicy messageSeparationPolicy =
            new NoSeparationPolicy();

    public FountainMQ getFmq() {
        return fmq;
    }

    public void setFmq(FountainMQ fmq) {
        this.fmq = fmq;
    }

    @Override
    public void transport(Object event) throws DispatchException {
        sendWithSeparation(event, true);
    }

    @Override
    public void transport(Object event, boolean mustSend)
            throws DispatchException {
        sendWithSeparation(event, mustSend);
    }

    @Override
    public void registerProducer(String producer) {

    }

    /**
     * 分解消息并且发送
     *
     * @param event    消息
     * @param mustSend 是否必须发送
     */
    private void sendWithSeparation(final Object event, final boolean mustSend) {
        Iterator<Object> it = messageSeparationPolicy.separate(event);
        // List<Object> all = new ArrayList<Object>();
        while (it.hasNext()) {
            Object o = it.next();
            // all.add(o);
            pushEvent(o, mustSend);
        }
        // comp(all,event);
    }

    // private void comp(List<Object> all,final Object event){
    // List<RowData> left = new ArrayList<RowData>();
    // int leftCount = 0;
    // int leftCount1 = 0;
    // for(Object o : all){
    // ChangeDataSet ds = (ChangeDataSet)o;
    // leftCount += ds.getDataSize();
    // for(String name : ds.getTableDef().keySet()){
    // leftCount1 += ds.getTableData().get(name).size();
    // left.addAll(ds.getTableData().get(name));
    // }
    // }
    // List<RowData> right = new ArrayList<RowData>();
    // ChangeDataSet es = (ChangeDataSet)event;
    // for(String name : es.getTableDef().keySet()){
    // right.addAll(es.getTableData().get(name));
    // }
    // boolean ok = true;
    //
    // for(int i =0; i < left.size(); i++){
    // if(left.get(i) != right.get(i)){
    // ok = false;
    // break;
    // }
    // }
    // System.out.println(ok);
    // }

    /**
     * 检查事件是否是BinlogTraceable
     *
     * @param event 事件数据
     *
     * @return BinlogTraceable
     */
    private BinlogTraceable checkIfBinlogTraceable(Object event) {
        if (event instanceof BinlogTraceable) {
            return (BinlogTraceable) event;
        }
        throw new DispatchException("event must be BinlogTraceable");
    }

    /**
     * 发送数据
     *
     * @param event    事件数据
     * @param mustSend 是否必须发送
     */
    private void pushEvent(Object event, boolean mustSend) {
        BinlogTraceable e = checkIfBinlogTraceable(event);
        if (fmq.isExceedMaxCapacity(e.getDataSize())) {
            throw new DispatchException("transaction is exceed max size:"
                    + e.getDataSize());
        } else {
            if (mustSend) {
                fmq.push(e);
            } else {
                fmq.push(e, 0);
            }
        }
    }

    public MessageSeparationPolicy getMessageSeparationPolicy() {
        return messageSeparationPolicy;
    }

    public void setMessageSeparationPolicy(
            MessageSeparationPolicy messageSeparationPolicy) {
        this.messageSeparationPolicy = messageSeparationPolicy;
    }

}

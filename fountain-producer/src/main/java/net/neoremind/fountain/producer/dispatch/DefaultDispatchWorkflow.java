package net.neoremind.fountain.producer.dispatch;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.pack.PackProtocol;
import net.neoremind.fountain.pack.impl.NonePackProtocol;

/**
 * <b>缺省的DispatchWorkflow实现</b>。该实现中定制类如下流程：
 * <ul>
 * <li>转换,使用{@link net.neoremind.fountain.producer.dispatch.EventConverter EventConverter}接口把
 * {@link ChangeDataSet ChangeDataSet}类型转化为更为友好的java类型
 * </li>
 * <li>打包,将上一步骤的结果使用{@link net.neoremind.fountain.producer.dispatch.PackProtoco PackProtoco}接口打包成可传输的数据类型</li>
 * <li>传输前过滤，在传输前调用{@link TransportFilter TransportFilter}接口进行过滤，通过过滤条件的才能继续传输</li>
 * <li>传输,使用{@link Transport Transport}进行下发数据</li>
 * </ul>
 *
 * @author hexiufeng, zhangxu
 */
public class DefaultDispatchWorkflow implements DispatchWorkflow {
    /**
     * 打包协议实现，缺省实现不做任何处理，一般用于producer和consumer部署在同一jvm中
     */
    private PackProtocol packProtocol = new NonePackProtocol();
    /**
     * 过滤器，在真正下发传输之前最后一次决定是否发送的机会
     */
    private TransportFilter transFilter = new NullTransportFilter();
    /**
     * 下发传输器
     */
    private Transport tranport;

    public Transport getTranport() {
        return tranport;
    }

    public void setTranport(Transport tranport) {
        this.tranport = tranport;
    }

    public PackProtocol getPackProtocol() {
        return packProtocol;
    }

    public void setPackProtocol(PackProtocol packProtocol) {
        this.packProtocol = packProtocol;
    }

    public TransportFilter getTransFilter() {
        return transFilter;
    }

    public void setTransFilter(TransportFilter transFilter) {
        this.transFilter = transFilter;
    }

    @Override
    public void dispatchEvent(ChangeDataSet ds) throws DispatchException {
        if (packProtocol == null) {
            throw new DispatchException("null pack protocol.");
        } else {
            if (transFilter.filter(ds)) {
                tranport.transport(packProtocol.pack(ds));
            }
        }
    }

    @Override
    public void registerProducer(String producer) {
        if (tranport != null) {
            tranport.registerProducer(producer);
        }
    }
}

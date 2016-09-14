package net.neoremind.fountain.producer.dispatch;

import java.util.List;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.producer.DispatchFailedStrategy;

/**
 * 组装模式，可以向一组DispatchWorkflow发送数据
 *
 * @author hexiufeng
 */
public class GroupDispatchWorkflow implements DispatchWorkflow {
    /**
     * 下发流程列表，一个数据的变化可以一次发送到不同的目的地， 每个
     * {@link DispatchWorkflow
     * DispatchWorkflow}代表不同发送方式和发送目的地
     */
    private List<DispatchWorkflow> dispList;
    /**
     * 下发失败控制策略，缺省直接抛出异常
     */
    private DispatchFailedStrategy dispatchFailedStrategy =
            new DispatchFailedStrategy() {

                @Override
                public void handleFailed(DispatchWorkflow sender,
                                         ChangeDataSet ds) {
                    throw new RuntimeException();
                }
            };

    public List<DispatchWorkflow> getDispList() {
        return dispList;
    }

    public void setDispList(List<DispatchWorkflow> dispList) {
        this.dispList = dispList;
    }

    public DispatchFailedStrategy getDispatchFailedStrategy() {
        return dispatchFailedStrategy;
    }

    public void setDispatchFailedStrategy(
            DispatchFailedStrategy dispatchFailedStrategy) {
        this.dispatchFailedStrategy = dispatchFailedStrategy;
    }

    @Override
    public void dispatchEvent(ChangeDataSet ds) throws DispatchException {
        for (DispatchWorkflow disp : dispList) {
            try {
                disp.dispatchEvent(ds);
            } catch (DispatchException exp) {
                dispatchFailedStrategy.handleFailed(disp, ds);
            }
        }

    }

    @Override
    public void registerProducer(String producer) {
        for (DispatchWorkflow workflow : dispList) {
            if (workflow != null) {
                workflow.registerProducer(producer);
            }
        }
    }

}

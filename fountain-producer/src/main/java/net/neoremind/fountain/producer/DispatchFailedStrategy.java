package net.neoremind.fountain.producer;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.producer.dispatch.DispatchWorkflow;

/**
 * 下发失败时，异常控制策略
 *
 * @author hexiufeng
 */
public interface DispatchFailedStrategy {
    /**
     * 处理下发失败后的后续工作
     *
     * @param sender
     * @param ds
     */
    void handleFailed(DispatchWorkflow sender, ChangeDataSet ds);
}

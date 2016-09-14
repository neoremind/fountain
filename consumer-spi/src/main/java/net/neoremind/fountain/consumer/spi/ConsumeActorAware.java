package net.neoremind.fountain.consumer.spi;

/**
 * 知晓ConsumeActor元接口
 *
 * @author zhangxu
 */
public interface ConsumeActorAware {

    /**
     * Set the ConsumeActor that this object runs in.
     *
     * @param consumeActor
     */
    void setConsumeActor(ConsumeActor consumeActor);

}

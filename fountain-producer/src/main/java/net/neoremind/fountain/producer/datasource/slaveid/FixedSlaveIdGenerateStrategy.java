package net.neoremind.fountain.producer.datasource.slaveid;

/**
 * 固定的slaveId
 *
 * @author zhangxu
 */
public class FixedSlaveIdGenerateStrategy implements SlaveIdGenerateStrategy<Integer> {

    private int slaveId;

    @Override
    public Integer get() {
        return slaveId;
    }

    public int getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(int slaveId) {
        this.slaveId = slaveId;
    }
}

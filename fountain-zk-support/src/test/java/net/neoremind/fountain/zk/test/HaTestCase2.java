package net.neoremind.fountain.zk.test;

import org.junit.Test;

/**
 * 测试leader elector
 *
 * @author zhangxu
 */
public class HaTestCase2 extends BaseHaTestCase {

    @Override
    protected String getInstanceName() {
        return "testcase2";
    }

    @Test
    public void getLeaderTest() throws Exception {
        tryGetLeadership();
    }
}

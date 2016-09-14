package net.neoremind.fountain.zk.test;

import org.junit.Test;

/**
 * 测试leader elector
 *
 * @author zhangxu
 */
public class NegativeHaTestCase extends BaseHaTestCase {

    @Override
    public String getZkString() {
        return "127.0.0.1:80";
    }

    @Override
    protected String getInstanceName() {
        return "negativetestcase";
    }

    @Test
    public void getLeaderTest() throws Exception {
        tryGetLeadership();
    }
}

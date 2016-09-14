package net.neoremind.fountain.test.it;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import net.neoremind.fountain.test.it.template.singletable.OneTransMultiInsertRowTest;
import net.neoremind.fountain.test.it.template.singletable.OneTransOneInsertRowTest;

/**
 * AllTestSuite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
        {OneTransOneInsertRowTest.class,
                OneTransMultiInsertRowTest.class}
)
// FIXME 目前还不能跑起来，需要单独运行每个testcase
public class AllTestSuite {

}

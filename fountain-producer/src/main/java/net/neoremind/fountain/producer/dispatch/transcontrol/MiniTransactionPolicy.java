package net.neoremind.fountain.producer.dispatch.transcontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.producer.dispatch.TableDataProvider;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

/**
 * 完整小事务控制策略<br>
 * <b>注意</b><br>
 * <p/>
 * <ul>
 * <li>事务的整体有上限，上限的单位是修改数据的条数，如果超过指定的条数会忽略掉该事务的数据，默认条数时30000</li>
 * <li>使用本策略同时使用内存mq时，请注意设置一个合理的内存mq容量，否则容易引起内存gc问题</li>
 * <li>不适用用databus5.5</li>
 * </ul>
 *
 * @author hexiufeng
 */
@UnThreadSafe
public class MiniTransactionPolicy extends AbstractFullTransactionPolicy
        implements TransactionPolicy {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MiniTransactionPolicy.class);

    private int transLen = 0;

    private boolean shouldOutLog = true;

    @Override
    protected int getTransLen() {
        return transLen;
    }

    @Override
    protected boolean isCanAddEvent(ChangeDataSet ds,
                                    TableDataProvider provider, TableSizeEntry entry) {

        boolean add =
                getTransLen() + provider.getDataLen() < super.getMaxTransSize();

        transLen += provider.getDataLen();
        if (!add && shouldOutLog) {
            shouldOutLog = false;
            if (provider.getGTId() != null) {
                LOGGER.warn("Huge transaction table, data size is more than {}, gt id is {}, so the increments will "
                        + "be ignored", super.getMaxTransSize(), provider.getGTId());
            }
        }
        return add;
    }

    @Override
    public void clear() {
        super.clear();
        transLen = 0;
        shouldOutLog = true;
    }

}

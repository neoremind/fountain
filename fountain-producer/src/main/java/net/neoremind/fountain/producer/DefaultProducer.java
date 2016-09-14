package net.neoremind.fountain.producer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.exception.DataErrorException;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogDumpStrategyAware;
import net.neoremind.fountain.rowbaselog.event.BinlogEventHeader;
import net.neoremind.fountain.rowbaselog.event.QueryLogEvent;
import net.neoremind.fountain.rowbaselog.event.XidLogEvent;
import net.neoremind.fountain.support.ThreadHolder;
import net.neoremind.fountain.support.TrxContext;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

/**
 * 基于row base binlog 的数据源监控实现
 *
 * @author zhangxu
 */
@UnThreadSafe
public class DefaultProducer extends AbstractProducer implements
        SingleProducer, BeanNameAware {

    private static final Logger logger = LoggerFactory
            .getLogger(DefaultProducer.class);

    @Override
    protected boolean procEventData(byte[] data, BaseLogEvent[] eventHolder) {
        BaseLogEvent event = null;
        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        BinlogEventHeader header = null;
        try {
            header = getParser().parseHeader(buf);
            event = getParser().parseDataToEvent(buf, header);
        } catch (DataErrorException e) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "WARNING ERROR-fountain:parse data failed, fountain will ignore this event, trxContext is "
                                + ThreadHolder.getTrxContext(), e);
            }
            return false;
        } catch (RuntimeException e) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "WARNING ERROR-fountain:parse data failed, fountain will ignore this event, trxContext is "
                                + ThreadHolder.getTrxContext(), e);
            }
            return false;
        }

        if (event == null) {
            return false;
        }
        eventHolder[0] = event;
        return true;
    }

    private String extractGroupIdFromHeader(BinlogEventHeader header) {
        if (header == null) {
            return "";
        }
        return header.getGroupId() == null ? "null" : header.getGroupId()
                .toString();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void endTrans(BaseLogEvent event) {
        if (event instanceof XidLogEvent) {
            getDataSource().persitSyncPoint(createSyncPoint(event));
            return;
        }
        if (event instanceof QueryLogEvent) {
            String query = ((QueryLogEvent) event).query;
            if (StringUtils.isEmpty(query)) {
                return;
            }
            query = query.toLowerCase().trim();
            if (query.equals("rollback")) {
                getDataSource().persitSyncPoint(createSyncPoint(event));
            }
        }
    }

    private SyncPoint createSyncPoint(BaseLogEvent event) {
        TrxContext trxContext = ThreadHolder.getTrxContext();
        return ((BinlogDumpStrategyAware) getDataSource()).getBinlogDumpStrategy()
                .createCurrentSyncPoint(this.getDataSource(),
                        trxContext.getSid(),
                        trxContext.getCurrGtId(),
                        trxContext.getBinlogFileName(),
                        trxContext.getNextBinlogPosition());
    }

    @Override
    protected void fillSyncPoint(ChangeDataSet ds, BaseLogEvent event) {
        ds.setSyncPoint(createSyncPoint(event));
    }

}

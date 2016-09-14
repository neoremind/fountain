package net.neoremind.fountain.producer.datasource.eventpositionext;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.datasource.MysqlDataSource;
import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint;
import net.neoremind.fountain.eventposition.EventPositionExtender;
import net.neoremind.fountain.eventposition.GroupIdSyncPoint;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.packet.FieldDescriptionPacket;
import net.neoremind.fountain.packet.ResultSetPacket;
import net.neoremind.fountain.packet.RowValuePacket;
import net.neoremind.fountain.thread.annotaion.ThreadSafe;
import net.neoremind.fountain.util.CollectionUtils;

/**
 * 由gt id扩展获取mysql binlog filename and postion的扩展器
 *
 * @author hexiufeng
 */
@ThreadSafe
public class GtId2BinPositionEventPositionExtender implements
        EventPositionExtender {
    private static final Logger logger = LoggerFactory
            .getLogger(GtId2BinPositionEventPositionExtender.class);

    @Override
    public BinlogAndOffsetSyncPoint extend(SyncPoint groupIdPoint,
                                           MysqlDataSource dataSource) throws IOException, NoSuchAlgorithmException {

        if (groupIdPoint instanceof BinlogAndOffsetSyncPoint) {
            return (BinlogAndOffsetSyncPoint) groupIdPoint;
        }

        // 调用show binlog info for “gt id” 来获取其他信息
        String queryString = "show binlog info for " + getGroupId(groupIdPoint);
        logger.info(queryString);
        ResultSetPacket resultSetPacket = dataSource.query(queryString);
        if (resultSetPacket == null) {
            return null;
        }

        List<FieldDescriptionPacket> fieldDescriptionList =
                resultSetPacket.getFieldDescriptionList();
        List<RowValuePacket> rowValueList = resultSetPacket.getRowValueList();
        if (CollectionUtils.isEmpty(fieldDescriptionList)
                || CollectionUtils.isEmpty(rowValueList)) {
            return null;
        }

        for (RowValuePacket rowPacket : rowValueList) {
            List<String> fieldValueList = rowPacket.getFieldValueList();
            if (CollectionUtils.isEmpty(fieldValueList)
                    || fieldValueList.size() != 3) {
                continue;
            }

            BinlogAndOffsetSyncPoint newEventPosition =
                    new BinlogAndOffsetSyncPoint();
            newEventPosition.addSyncPoint(dataSource.getIpAddress(),
                    dataSource.getPort(), fieldValueList.get(0),
                    BigInteger.valueOf(Long.parseLong(fieldValueList.get(1))));

            return newEventPosition;
        }

        return null;
    }

    private String getGroupId(SyncPoint groupIdPoint) {
        if (groupIdPoint instanceof GroupIdSyncPoint) {
            return ((GroupIdSyncPoint) groupIdPoint).offerGroupId()
                    .toString();
        }
        throw new RuntimeException("expect groupid point.");
    }
}

package net.neoremind.fountain.eventposition;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import net.neoremind.fountain.eventposition.AsyncFixedRateDisposeEventPosition;
import net.neoremind.fountain.eventposition.BaiduGroupIdSyncPoint;
import net.neoremind.fountain.eventposition.DelayGroupIdDisposeEventPosition;
import net.neoremind.fountain.eventposition.LocalFileDisposeEventPosition;
import net.neoremind.fountain.eventposition.SyncPoint;

/**
 * @author zhangxu
 */
public class DisposeEventPositionTest {

    private static final String ROOT_PATH = "/Users/baidu/work/fountain/test";
    private static final String INSTANCE_NAME = "test00";

    @Test
    public void testLocalFile() {
        LocalFileDisposeEventPosition disposeEventPosition = createLocalFileDisposeEventPosition();
        disposeEventPosition.registerInstance(INSTANCE_NAME);
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(100L)));
        SyncPoint syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(new String(syncPoint.toBytes()), is("100"));
    }

    @Test
    public void testDelayGroupId() {
        DelayGroupIdDisposeEventPosition disposeEventPosition = new DelayGroupIdDisposeEventPosition();
        disposeEventPosition.setDelegate(createLocalFileDisposeEventPosition());
        disposeEventPosition.registerInstance(INSTANCE_NAME);

        // 1st
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(100L)));
        SyncPoint syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(syncPoint, nullValue());

        // 2nd
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(100L)));
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(syncPoint, nullValue());

        // 3rd
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(200L)));
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(new String(syncPoint.toBytes()), is("100"));

        // 4th
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(300L)));
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(new String(syncPoint.toBytes()), is("200"));
    }

    @Test
    public void testAsyncFixedRate() throws Exception {
        AsyncFixedRateDisposeEventPosition disposeEventPosition = new AsyncFixedRateDisposeEventPosition();
        disposeEventPosition.setInitDelayMs(2000L);
        disposeEventPosition.setPeriodMs(2000L);
        disposeEventPosition.setDelegate(createLocalFileDisposeEventPosition());
        disposeEventPosition.registerInstance(INSTANCE_NAME);
        disposeEventPosition.init();

        // 立即存储，会延迟存储
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(100L)));
        SyncPoint syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(syncPoint, nullValue());

        // 1秒后没存上
        Thread.sleep(500L);
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(syncPoint, nullValue());

        // 3秒后应该异步存上了
        Thread.sleep(2000L);
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(new String(syncPoint.toBytes()), is("100"));

        // 6秒后由于id相同不存储
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(100L)));
        Thread.sleep(3000L);
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(new String(syncPoint.toBytes()), is("100"));

        // 9秒后存储了200
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(200L)));
        Thread.sleep(3000L);
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(new String(syncPoint.toBytes()), is("200"));
    }

    @Test
    public void testDelayGroupIdAsyncFixedRate() throws Exception {
        DelayGroupIdDisposeEventPosition disposeEventPosition = new DelayGroupIdDisposeEventPosition();
        AsyncFixedRateDisposeEventPosition asyncFixedRateDisposeEventPosition =
                new AsyncFixedRateDisposeEventPosition();
        asyncFixedRateDisposeEventPosition.setInitDelayMs(2000L);
        asyncFixedRateDisposeEventPosition.setPeriodMs(2000L);
        asyncFixedRateDisposeEventPosition.setDelegate(createLocalFileDisposeEventPosition());
        asyncFixedRateDisposeEventPosition.init();
        disposeEventPosition.setDelegate(asyncFixedRateDisposeEventPosition);
        disposeEventPosition.registerInstance(INSTANCE_NAME);

        // 立即存储，会延迟存储
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(100L)));
        SyncPoint syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(syncPoint, nullValue());

        // 1秒后没存上
        Thread.sleep(500L);
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(syncPoint, nullValue());

        // 3秒后应该异步存上了,但是delay了一把，所以还是null
        Thread.sleep(2000L);
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(syncPoint, nullValue());

        // 6秒后由于id相同不存储
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(100L)));
        Thread.sleep(3000L);
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(syncPoint, nullValue());

        // 9秒后存储了200
        disposeEventPosition.saveSyncPoint(new BaiduGroupIdSyncPoint(BigInteger.valueOf(200L)));
        Thread.sleep(3000L);
        syncPoint = disposeEventPosition.loadSyncPoint();
        System.out.println(syncPoint);
        assertThat(new String(syncPoint.toBytes()), is("100"));
    }

    private LocalFileDisposeEventPosition createLocalFileDisposeEventPosition() {
        LocalFileDisposeEventPosition disposeEventPosition = new LocalFileDisposeEventPosition();
        disposeEventPosition.setRootPath(ROOT_PATH);
        return disposeEventPosition;
    }

    @Before
    public void clear() throws IOException {
        FileUtils.deleteQuietly(new File(ROOT_PATH + File.separator + INSTANCE_NAME));
    }

}

package net.neoremind.fountain.eventposition;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.neoremind.fountain.eventposition.GtId;
import net.neoremind.fountain.eventposition.GtIdSet;
import net.neoremind.fountain.exception.GtIdInvalidException;
import net.neoremind.fountain.util.StringPool;
import com.google.common.collect.Lists;

/**
 * @author zhangxu
 */
public class GtIdSetTest {

    @Test
    public void testToStringSingle() {
        GtIdSet gtIdSet = getSingle();
        System.out.println(gtIdSet);
        assertThat(gtIdSet.toString(), is("31a25a80-eee5-11e4-9dfd-90a380967173:1-655"));
        assertThat(gtIdSet.getGtIdList().get(0).getServerUUIDbyte()[2], is((byte) 0x5a));
        assertThat(gtIdSet.getGtIdList().get(0).getServerUUIDbyte()[15], is((byte) 0x73));
    }

    @Test
    public void testFromStringSingle() {
        GtIdSet gtIdSet = GtIdSet.buildFromString("31a25a80-eee5-11e4-9dfd-90a380967173:1-655");
        System.out.println(gtIdSet);
        assertThat(gtIdSet.toString(), is("31a25a80-eee5-11e4-9dfd-90a380967173:1-655"));
    }

    @Test
    public void testToStringMulti() {
        GtIdSet gtIdSet = getMulti();
        System.out.println(gtIdSet);
        assertThat(gtIdSet.toString(), is("31a25a80-eee5-11e4-9dfd-90a380967173:1-655" +
                StringPool.Symbol.COMMA + "3f149314-9cad-11e5-8b30-00259089db03:777-888"));
    }

    @Test
    public void testFromStringMulti() {
        GtIdSet gtIdSet = GtIdSet.buildFromString("31a25a80-eee5-11e4-9dfd-90a380967173:1-655" +
                StringPool.Symbol.COMMA + "3f149314-9cad-11e5-8b30-00259089db03:777-888");
        System.out.println(gtIdSet);
        assertThat(gtIdSet.toString(), is("31a25a80-eee5-11e4-9dfd-90a380967173:1-655" +
                StringPool.Symbol.COMMA + "3f149314-9cad-11e5-8b30-00259089db03:777-888"));
    }

    @Test(expected = GtIdInvalidException.class)
    public void testToStringSingleNegative() {
        GtIdSet gtIdSet = GtIdSet.buildFromString("31a25a80-eee5-11e4-9dfd-90a3809673:1-655");
    }

    @Test(expected = GtIdInvalidException.class)
    public void testToStringSingleNegative2() {
        GtIdSet gtIdSet = GtIdSet.buildFromString("31a25a80-eee5-11e4-9dfd-90a380967173:1-y");
    }

    @Test(expected = GtIdInvalidException.class)
    public void testToStringSingleNegative3() {
        GtIdSet gtIdSet = GtIdSet.buildFromString("31a25a80-eee5-11e4-9dfd-90a380967173:1");
    }

    @Test(expected = GtIdInvalidException.class)
    public void testToStringSingleNegative4() {
        GtIdSet gtIdSet = GtIdSet.buildFromString("31a25a80-eee5-11e4-9dfd-90a380967173:99-1");
    }

    private GtIdSet getSingle() {
        GtIdSet gtIdSet = new GtIdSet();
        GtId gtId = new GtId("31a25a80-eee5-11e4-9dfd-90a380967173", 1L, 655L);
        gtIdSet.setGtIdList(Lists.newArrayList(gtId));
        return gtIdSet;
    }

    private GtIdSet getMulti() {
        GtIdSet gtIdSet = new GtIdSet();
        GtId gtId1 = new GtId("31a25a80-eee5-11e4-9dfd-90a380967173", 1L, 655L);
        GtId gtId2 = new GtId("3f149314-9cad-11e5-8b30-00259089db03", 777L, 888);
        gtIdSet.setGtIdList(Lists.newArrayList(gtId1, gtId2));
        return gtIdSet;
    }

}

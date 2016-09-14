package net.neoremind.fountain.event;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public interface EventHeader {
    BigInteger getGroupId();

    long getTimestamp();

    void parseHeader(ByteBuffer buf);
}

package net.neoremind.fountain.meta;

/**
 * databus定义的列bitmap flag
 *
 * @author zhangxu
 */
public enum DatabusColBitmapFlag {

    /**
     * Field can't be NULL
     */
    NOT_NULL_FLAG(1),

    /**
     * Field is part of a primary key
     */
    PRI_KEY_FLAG(2),

    /**
     * Field is part of a unique key
     */
    UNIQUE_KEY_FLAG(4),

    /**
     * Field is part of a key
     */
    MULTIPLE_KEY_FLAG(8),

    /**
     * Field is a blob
     */
    BLOB_FLAG(16),

    /**
     * Field is unsigned
     */
    UNSIGNED_FLAG(32),

    /**
     * Field is zerofill
     */
    ZEROFILL_FLAG(64),

    /**
     * Field is binary
     */
    BINARY_FLAG(128);

    private DatabusColBitmapFlag(final int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}

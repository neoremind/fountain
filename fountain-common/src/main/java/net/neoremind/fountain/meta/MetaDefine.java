package net.neoremind.fountain.meta;

/**
 * 数据库原信息定义
 *
 * @author zhangxu
 */
public class MetaDefine {

    /**
     * <code>show full fields from TABLE</code>命令中的索引<tt>Key</tt>列的定义
     */
    public interface KEY_FLAG {

        /**
         * 主键或者主键组成部分
         */
        String PRIMARY_KEY = "PRI";

        /**
         * 列值可重复
         * <p/>
         * 该列是一个非唯一索引的前导列（第一列）或者是一个唯一性索引的组成部分，可以含有空值NULL
         */
        String MULTIPLE_KEY = "MUL";

        /**
         * 唯一索引的第一列（前导列），并别不能含有空值NULL
         */
        String UNIQUE_KEY = "UNI";

        /**
         * 该列值的可以重复，表示该列没有索引，或者是一个非唯一的复合索引的非前导列
         */
        String EMPTY = "";
    }

    /**
     * <code>show full fields from TABLE</code>命令中的索引<tt>Null</tt>列的定义
     */
    public interface NOT_NULL_FLAG {

        /**
         * 可为空
         */
        String YES = "YES";

        /**
         * 不可为空
         */
        String NO = "NO";

    }

}

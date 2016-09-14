package net.neoremind.fountain.packet;

import net.neoremind.fountain.util.CommandTypeConstant;

/**
 * 执行sql指令的命令包
 *
 * @author hanxu, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/com-query.html">com-query</a>
 * @since 2013年8月3日
 */
public abstract class CommandPacket extends MysqlPacket {

    private static final long serialVersionUID = -2746732761827410994L;

    /**
     * 基本上目前就是固定使用{@link CommandTypeConstant#COM_QUERY}
     */
    private byte command;

    /**
     * 待执行SQL，可以是insert、update、delete
     */
    private String sql;

    public byte getCommand() {
        return command;
    }

    protected void setCommand(byte command) {
        this.command = command;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

}

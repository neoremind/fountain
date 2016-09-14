package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * @author zhangxu
 */
public class QueryLogEvent extends BaseLogEvent {
	
	private static final long serialVersionUID = -1567353892593172493L;
	
	/**
	 * 
Query_log_event/QUERY_EVENT

Fixed data part:

4 bytes. The ID of the thread that issued this statement. 
Needed for temporary tables. This is also useful for a DBA for knowing who did what on the master.

4 bytes. The time in seconds that the statement took to execute. Only useful for inspection by the DBA.

1 byte. The length of the name of the database which was the default database when the statement was executed. 
This name appears later, in the variable data part. 
It is necessary for statements such as INSERT INTO t VALUES(1) that don't specify the database 
	and rely on the default database previously selected by USE.

2 bytes. The error code resulting from execution of the statement on the master. 
Error codes are defined in include/mysqld_error.h. 0 means no error. 
How come statements with a nonzero error code can exist in the binary log? 
This is mainly due to the use of nontransactional tables within transactions. 
For example, if an INSERT ... SELECT fails after inserting 1000 rows into a MyISAM table (for example, with a duplicate-key violation), 
	we have to write this statement to the binary log, because it truly modified the MyISAM table. 
For transactional tables, there should be no event with a nonzero error code (though it can happen, 
	for example if the connection was interrupted (Control-C)). 
The slave checks the error code: After executing the statement itself, it compares the error code it got with the error code in the event, 
	and if they are different it stops replicating (unless --slave-skip-errors was used to ignore the error).

2 bytes (not present in v1, v3). The length of the status variable block.

Variable part:

Zero or more status variables (not present in v1, v3). 
Each status variable consists of one byte code identifying the variable stored, followed by the value of the variable. 
The format of the value is variable-specific, as described later.

The default database name (null-terminated).

The SQL statement. The slave knows the size of the other fields in the variable part (the sizes are given in the fixed data part), 
	so by subtraction it can know the size of the statement.

Each entry in the status variable block has a code and a value, where the value format is as indicated in the following list. 
The list provides basic information about each variable. For additional details, see log_event.h.

---------------------------------------------
Q_FLAGS2_CODE = 0. Value is a 4-byte bit-field. This variable is written only as of MySQL 5.0.

Q_SQL_MODE_CODE = 1. Value is an 8-byte SQL mode value.

Q_CATALOG_CODE = 2. Value is the catalog name: a length byte followed by that many bytes, plus a terminating null byte. 
	This variable is present only in MySQL 5.0.0 to 5.0.3. 
	It was replaced with Q_CATALOG_NZ_CODE in MySQL 5.0.4 because the terminating null is unnecessary.

Q_AUTO_INCREMENT = 3. Value is two 2-byte unsigned integers 
	representing the auto_increment_increment and auto_increment_offset system variables. 
	This variable is present only if auto_increment is greater than 1.

Q_CHARSET_CODE = 4. Value is three 2-byte unsigned integers representing the character_set_client, 
	collation_connection, and collation_server system variables.

Q_TIME_ZONE_CODE = 5. Value is the time zone name: a length byte followed by that many bytes. 
	This variable is present only if the time zone string is non-empty.

Q_CATALOG_NZ_CODE = 6. Value is the catalog name: a length byte followed by that many bytes. 
	Value is always std. This variable is present only if the catalog name is non-empty.

Q_LC_TIME_NAMES_CODE = 7. Value is a 2-byte unsigned integer representing the lc_time_names number. 
	This variable is present only if the value is not 0 (that is, not en_US).

Q_CHARSET_DATABASE_CODE = 8. Value is a 2-byte unsigned integer representing the collation_database system variable.

Q_TABLE_MAP_FOR_UPDATE_CODE = 9. Value is 8 bytes representing the table map to be updated by a multiple-table update statement. 
	Each bit of this variable represents a table, and is set to 1 if the corresponding table is to be updated by the statement.

Table_map_for_update is used to evaluate the filter rules specified by --replicate-do-table / --replicate-ignore-table.
	 */
	
	public int threadId; // 4 bytes
	public long executeTime; // 4 bytes 单位秒  The time in seconds that the statement took to execute
	public byte dbNameLength;  // 1 byte 默认数据库名字的长度
	public int errorCode; // 2 bytes  0 即表示无错，当对myisam表操作时，由于没有事务保证，会有非0的errorcode出现
	public int statusVariableLength; // 2 bytes 
	
	public Map<String, Object> variableMap = new HashMap<String, Object>();
	public String dbName;
	public String query;
	
	public QueryLogEvent(BinlogEventHeader eventHeader) {
		super(eventHeader);
	}

    @Override
    public BaseLogEvent parseData(ByteBuffer buf) {
        /**
         * get fixed data part 
         */
        this.threadId = (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 4);
        this.executeTime = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 4);
        this.dbNameLength = (byte)UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 1);
        this.errorCode = (int)UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
        this.statusVariableLength = (int)UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
        
        parseStatusVar(buf);
        
        // get db name
        dbName = UnsignedNumberHelper.convertByteArray2String(MysqlValueHelper.getNullTerminatedByte(buf));
        
        // get query
        query = UnsignedNumberHelper.convertByteArray2String(MysqlValueHelper.getFixedBytes(buf, buf.remaining()));
        return this;
    }

    private void parseStatusVar(ByteBuffer buf) {
        /**
         * get variable part
         */
        if(statusVariableLength > 0){
            int variableEndPos = buf.position() + statusVariableLength;
            while(buf.position() < variableEndPos){
                byte variableFlag = (byte)UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 1);
                
                long value = -1;
                switch(variableFlag){
                    case EventConstant.Q_FLAGS2_CODE : {
                        value = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 4);
                        variableMap.put("flags2", value);
                        break;
                    }
                    case EventConstant.Q_SQL_MODE_CODE : {
                        value = UnsignedNumberHelper.convertLittleEndianLong(buf, 8);
                        variableMap.put("sql_mode", value);
                        break;
                    }
                    case EventConstant.Q_CATALOG_CODE : {
                        buf.position(buf.position() + 1);
                        String catalog = UnsignedNumberHelper.convertByteArray2String(MysqlValueHelper.getNullTerminatedByte(buf));
                        variableMap.put("catalog", catalog);
                        break;
                    }
                    case EventConstant.Q_AUTO_INCREMENT : {
                        value = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
                        variableMap.put("auto_increment_increment", value);
                        
                        value = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
                        variableMap.put("auto_increment_offset ", value);
                        break;
                    }
                    case EventConstant.Q_CHARSET_CODE : {
                        value = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
                        variableMap.put("character_set_client", value);
                        
                        value = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
                        variableMap.put("collation_connection", value);
                        
                        UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);;
                        variableMap.put("collation_server ", value);
                        break;
                    }
                    case EventConstant.Q_TIME_ZONE_CODE : {
                        int length = (int)UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 1);
                        
                        String time_zone = UnsignedNumberHelper.convertByteArray2String(MysqlValueHelper.getFixedBytes(buf, length));
                        variableMap.put("time_zone", time_zone);
                        break;
                    }
                    case EventConstant.Q_CATALOG_NZ_CODE : {
                        int length = (int)(int)UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 1);
                        
                        String catalog = UnsignedNumberHelper.convertByteArray2String(MysqlValueHelper.getFixedBytes(buf, length));
                        variableMap.put("catalog", catalog);
                        break;
                    }
                    case EventConstant.Q_LC_TIME_NAMES_CODE : {
                        value = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
                        variableMap.put("lc_time_names ", value);
                        break;
                    }
                    case EventConstant.Q_CHARSET_DATABASE_CODE : {
                        value = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
                        variableMap.put("collation_database ", value);
                        break;
                    }
                    case EventConstant.Q_TABLE_MAP_FOR_UPDATE_CODE : {
                        value = UnsignedNumberHelper.convertLittleEndianLong(buf, 8);
                        variableMap.put("table_map_for_update ", value);
                        break;
                    }
                }
                
            }
        }
    }
	
	
}

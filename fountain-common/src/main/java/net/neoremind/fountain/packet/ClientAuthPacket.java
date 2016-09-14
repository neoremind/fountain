package net.neoremind.fountain.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import net.neoremind.fountain.util.MysqlCommonConstants;
import net.neoremind.fountain.util.Encrypter;
import net.neoremind.fountain.util.ProtocolHelper;

/**
 * mysql登录验证包
 *
 * @author hexiufeng, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/plain-handshake.html">Protocol::HandshakeResponse</a>
 * @since 2013-7-10
 */
public class ClientAuthPacket extends MysqlPacket {
    private static final long serialVersionUID = 8370460395899023539L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     * <p/>
     * 为空表示不需要密码验证登陆
     */
    private String password;

    /**
     * 数据库名
     */
    private String databaseName;

    /**
     * 字符编码
     */
    private byte charsetNumber;

    /**
     * 20-byte random challenge，用于密码加密使用，加密算法详细见{@link Encrypter}
     */
    private byte[] scrumbleBuff;

    /**
     * 转化为bytes
     *
     * @return bytes
     *
     * @throws IOException              io异常
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     */
    public byte[] toBytes() throws IOException, NoSuchAlgorithmException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 1. client_flags(4)
        ProtocolHelper.writeUnsignedIntByLittleEndian(1 | 4 | 512 | 8192 | 32768, out);

        // 2. max_packet(4)
        ProtocolHelper.writeUnsignedIntByLittleEndian(MysqlCommonConstants.MAX_PACKET_LENGTH, out);

        // 3. charset(1)
        out.write(charsetNumber);

        // 4. filler(23) -- always 0x00...
        out.write(new byte[23]);

        // 5. username(n)
        ProtocolHelper.writeNullTerminatedString(username, out);

        // 6. slat(N)
        if (password == null || password.length() == 0) {
            out.write(0x00);
        } else {
            byte[] encryptPassword = Encrypter.encryptBySHA(password.getBytes(), scrumbleBuff);
            ProtocolHelper.writeLengthCodedBinary(encryptPassword, out);
        }

        // 7. databasename(n)
        if (databaseName != null) {
            ProtocolHelper.writeNullTerminatedString(databaseName, out);
        }

        return out.toByteArray();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public byte getCharsetNumber() {
        return charsetNumber;
    }

    public void setCharsetNumber(byte charsetNumber) {
        this.charsetNumber = charsetNumber;
    }

    public byte[] getScrumbleBuff() {
        return scrumbleBuff;
    }

    public void setScrumbleBuff(byte[] scrumbleBuff) {
        this.scrumbleBuff = scrumbleBuff;
    }

    @Override
    public void fromBytes(byte[] data) {
        // don nothing
    }

}

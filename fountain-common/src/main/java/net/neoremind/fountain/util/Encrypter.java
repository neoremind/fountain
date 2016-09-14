package net.neoremind.fountain.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <a href="http://dev.mysql.com/doc/internals/en/secure-password-authentication
 * .html#packet-Authentication::Native41">验证方法</a>
 *
 * @author hanxu, zhangxu
 */
public class Encrypter {

    /**
     * 算法如下：<br/>
     * SHA1( password ) XOR SHA1( "20-bytes random data from server" <concat> SHA1( SHA1( password ) ) )
     *
     * @param data  密码字节数组
     * @param seeds 20-byte random challenge
     *
     * @return 加密后的密码字节数组
     *
     * @throws NoSuchAlgorithmException
     */
    public static byte[] encryptBySHA(byte[] data, byte[] seeds) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] passwordHashStage1 = md.digest(data);
        md.reset();

        byte[] passwordHashStage2 = md.digest(passwordHashStage1);
        md.reset();

        md.update(seeds);
        md.update(passwordHashStage2);
        byte[] toBeXord = md.digest();
        int numToXor = toBeXord.length;
        for (int i = 0; i < numToXor; i++) {
            toBeXord[i] = (byte) (toBeXord[i] ^ passwordHashStage1[i]);
        }
        return toBeXord;
    }
}

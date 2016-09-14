package net.neoremind.fountain.runner;

/**
 * 环境变量工具类
 *
 * @author zhangxu
 */
public class SysPropUtil {

    /**
     * Get system property
     *
     * @param key -D parameter or shell defined system environment property
     *
     * @return system property
     */
    public static String getSystemProperty(String key) {
        String value;
        value = System.getProperty(key);
        if (value == null || value.length() == 0) {
            value = System.getenv(key);
            if (value == null || value.length() == 0) {
                return value;
            }
        }
        return value;
    }

}

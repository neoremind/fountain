package net.neoremind.fountain;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.neoremind.fountain.util.CollectionUtil;
import net.neoremind.fountain.util.CollectionUtils;

/**
 * 数据源的一种基于字符串的配置
 *
 * @author zhangxu
 */
public class DataSource {

    /**
     * 默认数据库端口
     */
    public static final Integer DEFAULT_PORT = 3306;

    /**
     * IP:PORT的字符串，按照逗号分隔，对于同一个数据源可以提供多个IP:PORT对，用于binlog-syncer内部做高可用切换
     * <p/>
     * 举例：
     * <pre>
     *     10.1.1.2:3306,10.1.1.3:3307
     * </pre>
     */
    private String ipPortString;

    /**
     * 用户名字符串，按照逗号分隔
     * <p/>
     * Note: 至少一个，如果是一个则默认{@link #ipPortString}都用这一个，否则需要数量上一一对应
     * <p/>
     * 举例：
     * <pre>
     *     admin123,admin456
     * </pre>
     */
    private String usernameString;

    /**
     * 密码字符串，按照逗号分隔
     * <p/>
     * Note: 至少一个，如果是一个则默认{@link #ipPortString}都用这一个，否则需要数量上一一对应
     * <p/>
     * 举例：
     * <pre>
     *     pwd123,pwd456
     * </pre>
     */
    private String passwordString;

    /**
     * slaveId字符串，按照逗号分隔
     * <p/>
     * Note: <p/>
     * 1）可以为空，为空则采用随机生成策略。<p/>
     * 2）不为空则至少一个，如果是一个则默认{@link #ipPortString}都用这一个，否则需要数量上一一对应
     * <p/>
     * 举例：
     * <pre>
     *     pwd123,pwd456
     * </pre>
     */
    private String slaveIdString;

    /**
     * 构造方法
     *
     * @param ipPortString IP:PORT的字符串
     */
    public DataSource(String ipPortString) {
        this.ipPortString = ipPortString;
    }

    /**
     * 静态构造方法
     *
     * @param ipPortString IP:PORT的字符串
     */
    public static DataSource of(String ipPortString) {
        return new DataSource(ipPortString);
    }

    /**
     * 转换为entity实体，共内部更好的使用
     *
     * @return DataSourceEntity列表
     */
    public List<DataSourceEntity> toEntities() {
        List<DataSourceEntity> result = CollectionUtil.createArrayList(8);
        final List<P2<String, Integer>> ipPortList = toIpPortList();
        List<P1<String>> usernameList = toStringList(usernameString, "usernameString");
        List<P1<String>> passwordList = toStringList(passwordString, "passwordString");
        List<P1<Integer>> slaveIdList = toIntList(slaveIdString, "slaveIdString");
        P2<Integer, Integer> expected = new P2<Integer, Integer>() {
            @Override
            public Integer _1() {
                return 1;
            }

            @Override
            public Integer _2() {
                return ipPortList.size();
            }
        };
        checkMatch(expected, usernameList);
        checkMatch(expected, passwordList);

        for (int i = 0; i < ipPortList.size(); i++) {
            P2<String, Integer> ipPort = ipPortList.get(i);
            DataSourceEntity e = new DataSourceEntity();
            e.setIp(ipPort._1());
            e.setPort(ipPort._2());
            e.setUsername(getFirstOrByIndex(usernameList, i));
            e.setPassword(getFirstOrByIndex(passwordList, i));
            if (!CollectionUtil.isEmpty(slaveIdList)) {
                e.setSlaveId(getFirstOrByIndex(slaveIdList, i));
            }
            result.add(e);
        }
        Preconditions.checkState(!CollectionUtils.isEmpty(result), "Datasource should not be empty");
        return result;
    }

    /**
     * 获取{@code list}的第一个元素或者按照索引index获取。
     * 如果为空则返回{@code null}。
     *
     * @param list  列表
     * @param index 索引
     *
     * @return 对象
     */
    private <T> T getFirstOrByIndex(List<P1<T>> list, int index) {
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0)._1();
        }
        return list.get(index)._1();
    }

    /**
     * 检查{@code list}数量是否正确
     *
     * @param exptectedNum 预计的数量，包括2个
     * @param list         列表
     *
     * @throws IllegalStateException 如果不合法则抛出异常
     */
    private <T> void checkMatch(P2<Integer, Integer> exptectedNum, List<T> list) throws IllegalStateException {
        if (CollectionUtil.isEmpty(list) ||
                (list.size() != exptectedNum._1() && list.size() != exptectedNum._2())) {
            throw new IllegalStateException("Datasource argument not match");
        }
    }

    /**
     * 逗号
     */
    public static final String COMMA = ",";

    /**
     * 将IP:PORT列表转换为列表，列表中是的元素含有两个对象，一个是IP，一个是Port
     *
     * @return List<P2<String, Integer>> P2的一个是IP，一个是Port
     */
    private List<P2<String, Integer>> toIpPortList() {
        Preconditions.checkNotNull(ipPortString, "ipPort should not be empty");
        String[] ipPortArray = ipPortString.split(COMMA);
        List<P2<String, Integer>> result = CollectionUtil.createArrayList(8);
        for (String ipPort : ipPortArray) {
            String ip = ipPort;
            int port = DEFAULT_PORT;
            int pidx = ipPort.lastIndexOf(':');
            if (pidx >= 0) {
                if (pidx < ipPort.length() - 1) {
                    port = Integer.parseInt(ipPort.substring(pidx + 1));
                }
                ip = ipPort.substring(0, pidx);
            }
            final String finalIp = ip;
            final int finalPort = port;
            result.add(new P2<String, Integer>() {
                @Override
                public String _1() {
                    return finalIp;
                }

                @Override
                public Integer _2() {
                    return finalPort;
                }
            });
        }
        return result;
    }

    /**
     * 将字符串按照分隔符拆分为String列表
     *
     * @param str  字符串
     * @param name 字符串标示的名称，用于打印异常日志
     *
     * @return List<P1<String>>
     */
    private List<P1<String>> toStringList(String str, String name) {
        return toList(str, name, new F<String, String>() {
            @Override
            public String f(String s) {
                return s.trim();
            }
        });
    }

    /**
     * 将字符串按照分隔符拆分为整型列表
     *
     * @param str  字符串
     * @param name 字符串标示的名称，用于打印异常日志
     *
     * @return List<P1<String>>
     */
    private List<P1<Integer>> toIntList(String str, String name) {
        return toList(str, name, new F<String, Integer>() {
            @Override
            public Integer f(String s) {
                return Integer.parseInt(s.trim());
            }
        });
    }

    /**
     * 将字符串按照分隔符拆分为列表，并使用{@link DataSource.F}函数来做转换
     *
     * @param str  字符串
     * @param name 字符串标示的名称，用于打印异常日志
     * @param f    转换函数
     *
     * @return List<P1<T>>
     */
    private <T> List<P1<T>> toList(String str, String name, final F<String, T> f) {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyList();
        }

        String[] strArray = str.split(COMMA);
        List<P1<T>> result = CollectionUtil.createArrayList(8);
        for (final String s : strArray) {
            result.add(new P1<T>() {
                @Override
                public T _1() {
                    return f.f(s);
                }
            });
        }
        return result;
    }

    /**
     * 从A转成B的函数
     *
     * @param <A> 源对象类型
     * @param <B> 目标对象类型
     */
    interface F<A, B> {

        /**
         * 转换
         *
         * @param a 源对象
         *
         * @return 目标对象
         */
        B f(A a);
    }

    public String getIpPortString() {
        return ipPortString;
    }

    public DataSource ipPort(String ipPortString) {
        this.ipPortString = ipPortString;
        return this;
    }

    public String getUsernameString() {
        return usernameString;
    }

    public DataSource username(String usernameString) {
        this.usernameString = usernameString;
        return this;
    }

    public String getPasswordString() {
        return passwordString;
    }

    public DataSource password(String passwordString) {
        this.passwordString = passwordString;
        return this;
    }

    public String getSlaveIdString() {
        return slaveIdString;
    }

    public DataSource slaveId(String slaveIdString) {
        this.slaveIdString = slaveIdString;
        return this;
    }

    public void setIpPortString(String ipPortString) {
        this.ipPortString = ipPortString;
    }

    public void setUsernameString(String usernameString) {
        this.usernameString = usernameString;
    }

    public void setPasswordString(String passwordString) {
        this.passwordString = passwordString;
    }

    public void setSlaveIdString(String slaveIdString) {
        this.slaveIdString = slaveIdString;
    }

}

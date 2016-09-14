package net.neoremind.fountain.test.jmx;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.util.NetUtils;

import mx4j.tools.adaptor.http.HttpAdaptor;

/**
 * JMX MBean HTTP 适配器的实现类
 *
 * @author zhangxu
 */
public class MBeanHttpAdaptor implements MBeanAdaptor {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * HttpAdaptor sets the basic adaptor listening for HTTP requests
     */
    @Resource
    private HttpAdaptor httpAdaptor;

    /**
     * 简单认证需要的用户名，遵循HTTP标准安全认证规则
     */
    private String username;

    /**
     * 简单认证需要的口令，遵循HTTP标准安全认证规则
     */
    private String password;

    public void start() {
        // 发现主机名
        String hostname = NetUtils.getHostName();

        if (!NetUtils.LOCALHOST.equals(hostname)) {
            httpAdaptor.setHost(hostname);
        } else {
            // 若主机名为localhost，则换成IP地址
            String ip = NetUtils.getLocalHostIP();
            httpAdaptor.setHost(ip);
        }
        if (StringUtils.isNoneEmpty(username) && StringUtils.isNotEmpty(password)) {
            // 添加授权认证
            httpAdaptor.addAuthorization(username, password);
        }
        try {
            httpAdaptor.start();
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public void stop() {
        httpAdaptor.stop();
    }

    public void setHttpAdaptor(HttpAdaptor httpAdaptor) {
        this.httpAdaptor = httpAdaptor;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

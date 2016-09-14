package net.neoremind.fountain.datasource;

import java.net.Socket;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.meta.TableMeta;

/**
 * @author zhangxu
 */
@Ignore
public class AbstractMysqlDataSourceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMysqlDataSourceTest.class);

    @Test
    public void testQueryTableMeta() throws Exception {
        AbstractMysqlDataSource dataSource = new AbstractMysqlDataSource() {

            private Socket socket;

            @Override
            protected Logger getLogger() {
                return LOGGER;
            }

            @Override
            protected Socket createQuerySocket() {
                try {
                    socket = getNewSocket();
                    return updateSettings(socket);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void applySocket(Socket socket) {

            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() {
                closeSocket(socket);
            }
        };
        DatasourceConfigure conf = new DatasourceConfigure();
//        conf.setMysqlServer("10.94.37.23");
//        conf.setMysqlPort(8759);
//        conf.setUserName("beidou");
//        conf.setPassword("u7i8o9p0");

        conf.setMysqlServer("192.168.1.107");
        conf.setMysqlPort(3306);
        conf.setUserName("beidou");
        conf.setPassword("u7i8o9p0");
        dataSource.setConf(conf);
        TableMeta tableMeta = dataSource.queryTableMeta("fountain_test.student");
        System.out.println(tableMeta.getFullName());
        System.out.println(tableMeta.getTableId());
        System.out.println(tableMeta.getColumnMetaList());
    }
}

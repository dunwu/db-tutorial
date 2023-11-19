package io.github.dunwu.javadb.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

import java.io.IOException;

/**
 * HBase 工具实例化工厂
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-07-05
 */
public class HbaseFactory {

    public static HbaseTemplate newHbaseTemplate() throws IOException {
        return HbaseTemplate.newInstance(newHbaseConfiguration());
    }

    public static HbaseAdmin newHbaseAdmin() throws IOException {
        return HbaseAdmin.newInstance(newHbaseConfiguration());
    }

    public static Configuration newHbaseConfiguration() {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "127.0.0.1");
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.rootdir", "/hbase");
        configuration.set("hbase.meta.replicas.use", "true");
        configuration.set("hbase.client.retries.number", "5");
        configuration.set("hbase.rpc.timeout", "600000");
        return configuration;
    }

}

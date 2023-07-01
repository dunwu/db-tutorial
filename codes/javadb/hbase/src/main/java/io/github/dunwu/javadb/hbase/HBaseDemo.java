package io.github.dunwu.javadb.hbase;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;

@Slf4j
public class HBaseDemo {

    public static void main(String[] args) throws Exception {

        // 请改为配置的方式
        // String zkHosts = "192.168.31.127";
        String zkHosts = "192.168.31.255";
        // 请改为配置的方式
        String zkPort = "2181";
        // 请改为配置的方式
        String namespace = "test";
        String tablename = "test";
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", zkHosts);
        conf.set("hbase.zookeeper.port", zkPort);

        // 创建命名空间和表
        TableName tableName = TableName.valueOf(namespace, tablename);
        HBaseAdminHelper hbaseAdminHelper = HBaseAdminHelper.newInstance(conf);
        hbaseAdminHelper.enableTable(tableName);
        // hbaseAdminHelper.createNamespace(namespace);
        // hbaseAdminHelper.createTable(tableName, "c1");
        //
        // String rowKey = IdUtil.fastSimpleUUID();
        // HBaseHelper hbaseHelper = HBaseHelper.newInstance(hbaseAdminHelper.getConnection());
        // hbaseHelper.put(tableName, rowKey, "c1", "name", "jack");
        // String value = hbaseHelper.get(tableName, rowKey, "c1", "name");
        // System.out.println("value = " + value);

        hbaseAdminHelper.close();
    }

}

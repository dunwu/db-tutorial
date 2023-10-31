package io.github.dunwu.javadb.hbase;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * HBase {@link Admin} API 测试例
 * <p>
 * HBaseAdminHelper 是针对 {@link Admin} 常用 API 的封装工具类
 */
public class HBaseAdminHelperTests {

    private static HBaseAdminHelper hbaseAdminHelper = null;

    @BeforeAll
    public static void init() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "localhost");
        conf.set("hbase.zookeeper.port", "2181");
        hbaseAdminHelper = HBaseAdminHelper.newInstance(conf);
    }

    @AfterAll
    public static void destroy() {
        IoUtil.close(hbaseAdminHelper);
    }

    @Test
    @DisplayName("创建、删除、查看命名空间")
    public void testNamespace() throws IOException {
        // 创建命名空间
        hbaseAdminHelper.createNamespace("temp");
        dumpNamespaces();
        // 删除命名空间
        hbaseAdminHelper.dropNamespace("temp", true);
        dumpNamespaces();
    }

    private void dumpNamespaces() throws IOException {
        String[] namespaces = hbaseAdminHelper.listNamespaces();
        System.out.println("命名空间：");
        if (ArrayUtil.isNotEmpty(namespaces)) {
            Stream.of(namespaces).forEach(System.out::println);
        }
    }

    @Test
    @DisplayName("创建、删除、启用、禁用查看表")
    public void testTable() throws IOException {
        // 创建命名空间
        hbaseAdminHelper.createNamespace("temp");
        // 创建名为 test 的表，并含有两个列族 d 和 b
        hbaseAdminHelper.createTable(TableName.valueOf("temp:test"), "d", "b");
        // 查看表
        dumpTablesInNamespace("temp");
        // 禁用表
        hbaseAdminHelper.disableTable(TableName.valueOf("temp:test"));
        // 启用表
        hbaseAdminHelper.enableTable(TableName.valueOf("temp:test"));
        // 删除表
        hbaseAdminHelper.dropTable(TableName.valueOf("temp:test"));
        // 查看表
        dumpTablesInNamespace("temp");
        // 删除命名空间
        hbaseAdminHelper.dropNamespace("temp", true);
    }

    private void dumpTablesInNamespace(String namespace) throws IOException {
        TableName[] tableNames = hbaseAdminHelper.listTableNamesByNamespace(namespace);
        System.out.println("表：");
        if (ArrayUtil.isNotEmpty(tableNames)) {
            Stream.of(tableNames).forEach(System.out::println);
        }
    }

}

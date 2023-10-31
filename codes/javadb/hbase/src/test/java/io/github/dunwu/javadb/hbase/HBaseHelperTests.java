package io.github.dunwu.javadb.hbase;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import io.github.dunwu.javadb.hbase.entity.HBaseFamilyRequest;
import io.github.dunwu.javadb.hbase.entity.HBaseMultiFamilyRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Table;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HBase {@link Table} API 测试例
 * <p>
 * {@link HBaseHelper} 是针对 {@link Table} 常用 API 的封装工具类
 */
public class HBaseHelperTests {

    private static HBaseHelper hbaseHelper = null;
    private static HBaseAdminHelper hbaseAdminHelper = null;

    public static final String TABLE_NAME = "test:log";

    @BeforeAll
    public static void init() throws IOException {

        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "localhost");
        conf.set("hbase.zookeeper.port", "2181");
        hbaseHelper = HBaseHelper.newInstance(conf);
        hbaseAdminHelper = HBaseAdminHelper.newInstance(conf);

        // 创建命名空间
        hbaseAdminHelper.createNamespace("test");
        // 创建名为 test 的表，并含有两个列族 d 和 b
        hbaseAdminHelper.createTable(TableName.valueOf(TABLE_NAME), "d", "b");
    }

    @AfterAll
    public static void destroy() throws IOException {
        hbaseAdminHelper.dropTable(TableName.valueOf(TABLE_NAME));
        hbaseAdminHelper.dropNamespace("test", true);
        IoUtil.close(hbaseAdminHelper);
        IoUtil.close(hbaseHelper);
    }

    @Test
    public void test() throws Exception {
        String uuid = IdUtil.fastUUID();
        CommonInfo commonInfo = new CommonInfo(this.getClass().getCanonicalName(), "test", 100);
        LogInfo logInfo = new LogInfo("INFO", "hello world");
        hbaseHelper.put(TableName.valueOf(TABLE_NAME), uuid, "d", System.currentTimeMillis(), commonInfo);
        hbaseHelper.put(TableName.valueOf(TABLE_NAME), uuid, "b", System.currentTimeMillis(), logInfo);

        // 查单列的值
        String msg = hbaseHelper.getColumn(TableName.valueOf(TABLE_NAME), uuid, "b", "msg");
        String level = hbaseHelper.getColumn(TableName.valueOf(TABLE_NAME), uuid, "b", "level");
        Assertions.assertThat(msg).isEqualTo("hello world");
        Assertions.assertThat(level).isEqualTo("INFO");
        String className = hbaseHelper.getColumn(TableName.valueOf(TABLE_NAME), uuid, "d", "className");
        String methodName = hbaseHelper.getColumn(TableName.valueOf(TABLE_NAME), uuid, "d", "methodName");
        String lineNum = hbaseHelper.getColumn(TableName.valueOf(TABLE_NAME), uuid, "d", "lineNum");
        Assertions.assertThat(className).isEqualTo(this.getClass().getCanonicalName());
        Assertions.assertThat(methodName).isEqualTo("test");
        Assertions.assertThat(lineNum).isEqualTo("100");

        // 查单列族数据
        Map<String, String> familyB = hbaseHelper.getFamilyMap(TableName.valueOf(TABLE_NAME), uuid, "b");
        Map<String, String> familyD = hbaseHelper.getFamilyMap(TableName.valueOf(TABLE_NAME), uuid, "d");
        Assertions.assertThat(familyB).isNotEmpty();
        Assertions.assertThat(familyD).isNotEmpty();
        System.out.println("family b" + JSONUtil.toJsonStr(familyB));
        System.out.println("family d" + JSONUtil.toJsonStr(familyD));

        // 查多列族数据
        Map<String, Collection<String>> familyColumns = new HashMap<>(2);
        familyColumns.put("d", Arrays.asList("className", "methodName", "lineNum"));
        familyColumns.put("b", Arrays.asList("msg", "level"));
        Map<String, Map<String, String>> multiFamilyMap =
            hbaseHelper.getMultiFamilyMap(TableName.valueOf(TABLE_NAME), uuid, familyColumns);
        System.out.println("multiFamilyMap" + JSONUtil.toJsonStr(multiFamilyMap));

        HBaseMultiFamilyRequest request = new HBaseMultiFamilyRequest();
        request.setTableName(TABLE_NAME);
        request.setStartRow(uuid);
        request.getFamilyColumns().put("d", familyColumns.get("d"));
        request.getFamilyColumns().put("b", familyColumns.get("b"));
        Map<String, Map<String, Map<String, String>>> rowFamilyMap = hbaseHelper.scanMultiFamilyMap(request);
        System.out.println("rowFamilyMap" + JSONUtil.toJsonStr(rowFamilyMap));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class CommonInfo {

        private String className;
        private String methodName;
        private int lineNum;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class LogInfo {

        private String level;
        private String msg;

    }

}

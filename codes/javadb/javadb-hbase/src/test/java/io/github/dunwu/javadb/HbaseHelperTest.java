package io.github.dunwu.javadb;

import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Result;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Zhang Peng
 * @date 2019-03-29
 */
public class HbaseHelperTest {

    private static HbaseHelper hbaseHelper;

    @BeforeClass
    public static void BeforeClass() {
        try {
            hbaseHelper = new HbaseHelper();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listTable() throws Exception {
        HTableDescriptor[] hTableDescriptors = hbaseHelper.listTables();
        if (hTableDescriptors == null || hTableDescriptors.length <= 0) {
            Assert.fail();
        }

        System.out.println("Tables：");
        for (HTableDescriptor item : hTableDescriptors) {
            System.out.println(item.getTableName());
        }
    }

    @Test
    public void createTable() throws Exception {
        hbaseHelper.createTable("table1", new String[] {"columnFamliy1", "columnFamliy2"});
        HTableDescriptor[] table1s = hbaseHelper.listTables("table1");
        if (table1s == null || table1s.length <= 0) {
            Assert.fail();
        }

        hbaseHelper.createTable("table2", new String[] {"columnFamliy1", "columnFamliy2"});
        table1s = hbaseHelper.listTables("table2");
        if (table1s == null || table1s.length <= 0) {
            Assert.fail();
        }
    }

    @Test
    public void dropTable() throws Exception {
        hbaseHelper.dropTable("table1");
        HTableDescriptor[] table1s = hbaseHelper.listTables("table1");
        if (table1s != null && table1s.length > 0) {
            Assert.fail();
        }
    }


    @Test
    public void get() throws Exception {
        Result result = hbaseHelper.get("table1", "row1");
        System.out.println(hbaseHelper.resultToString(result));

        result = hbaseHelper.get("table1", "row2", "columnFamliy1");
        System.out.println(hbaseHelper.resultToString(result));
    }

    @Test
    public void scan() throws Exception {
        Result[] results = hbaseHelper.scan("table1");
        System.out.println("HbaseUtil.scan(\"table1\") result: ");
        if (results.length > 0) {
            for (Result r : results) {
                System.out.println(hbaseHelper.resultToString(r));
            }
        }

        results = hbaseHelper.scan("table1", "columnFamliy1");
        System.out.println("HbaseUtil.scan(\"table1\", \"columnFamliy1\" result: ");
        if (results.length > 0) {
            for (Result r : results) {
                System.out.println(hbaseHelper.resultToString(r));
            }
        }

        results = hbaseHelper.scan("table1", "columnFamliy1", "a");
        System.out.println("HbaseUtil.scan(\"table1\", \"columnFamliy1\", \"a\") result: ");
        if (results.length > 0) {
            for (Result r : results) {
                System.out.println(hbaseHelper.resultToString(r));
            }
        }
    }

    @Test
    public void delete() throws Exception {
        Result result = hbaseHelper.get("table1", "row1");
        System.out.println(result.toString());

        hbaseHelper.delete("table1", "row1");
        result = hbaseHelper.get("table1", "row1");
        System.out.println(result.toString());
    }
}

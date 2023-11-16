package io.github.dunwu.javadb.hbase;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import org.apache.hadoop.hbase.client.Put;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hbase Put 测试
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-13
 */
public class HbaseTemplatePutTest {

    public static final String TABLE_NAME = "test:test";

    private static final HbaseTemplate HBASE_TEMPLATE;

    static {
        try {
            HBASE_TEMPLATE = HbaseFactory.newHbaseTemplate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("put 测试 01")
    public void put01() throws IOException {
        long timestamp = System.currentTimeMillis();
        HBASE_TEMPLATE.put(TABLE_NAME, "test-key-0", "f1", "name", "user0");
        HBASE_TEMPLATE.put(TABLE_NAME, "test-key-1", timestamp, "f1", "name", "user1");
    }

    @Test
    @DisplayName("put 测试 02")
    public void put02() throws IOException {
        long timestamp = System.currentTimeMillis();
        User user2 = new User(2, "user2");
        HBASE_TEMPLATE.put(TABLE_NAME, "test-key-2", "f1", user2);
        User user3 = new User(3, "user3");
        HBASE_TEMPLATE.put(TABLE_NAME, "test-key-3", timestamp, "f1", user3);
    }

    @Test
    @DisplayName("put 测试 03")
    public void put03() throws IOException {
        long timestamp = System.currentTimeMillis();
        User user4 = new User(4, "user4");
        Map<String, Object> map = BeanUtil.beanToMap(user4);
        HBASE_TEMPLATE.put(TABLE_NAME, "test-key-4", timestamp, "f1", map);
    }

    @Test
    @DisplayName("put 测试 04")
    public void put04() throws IOException {
        long timestamp = System.currentTimeMillis();
        User user5 = new User(5, "user5");
        Product product5 = new Product("test-key-5", "product5", new BigDecimal(4000.0));
        Map<String, Map<String, Object>> familyMap = new HashMap<>(2);
        Map<String, Object> userMap = BeanUtil.beanToMap(user5);
        familyMap.put("f1", userMap);
        Map<String, Object> productMap = BeanUtil.beanToMap(product5);
        familyMap.put("f2", productMap);
        HBASE_TEMPLATE.put(TABLE_NAME, "test-key-5", timestamp, familyMap);
    }

    @Test
    @DisplayName("put 测试 05")
    public void put05() throws IOException {
        Put put = HbaseTemplate.newPut("test-key-6", null, "f1", "name", "user6");
        HBASE_TEMPLATE.put(TABLE_NAME, put);
    }

    @Test
    @DisplayName("put 测试 06")
    public void put06() throws IOException, InterruptedException {
        long timestamp = System.currentTimeMillis();
        User user7 = new User(5, "user7");
        Product product7 = new Product("test-key-7", "product5", new BigDecimal(4000.0));
        Put put1 = HbaseTemplate.newPut("test-key-7", timestamp, "f1", user7);
        Put put2 = HbaseTemplate.newPut("test-key-7", timestamp, "f2", product7);
        List<Put> list = Arrays.asList(put1, put2);
        HBASE_TEMPLATE.batchPut(TABLE_NAME, list);
    }

    @Test
    @DisplayName("batchPut 测试2")
    public void batchPut2() throws IOException, InterruptedException {
        Product product1 = new Product("test-key-8", "product8", new BigDecimal(4000.0));
        Product product2 = new Product("test-key-9", "product9", new BigDecimal(5000.0));
        List<Product> products = CollectionUtil.newArrayList(product1, product2);
        HBASE_TEMPLATE.batchPut(TABLE_NAME, "f2", products);
    }

}

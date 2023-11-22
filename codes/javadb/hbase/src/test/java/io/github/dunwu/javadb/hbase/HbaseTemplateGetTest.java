package io.github.dunwu.javadb.hbase;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseEntity;
import io.github.dunwu.javadb.hbase.entity.common.ColumnDo;
import io.github.dunwu.javadb.hbase.entity.common.FamilyDo;
import io.github.dunwu.javadb.hbase.entity.common.RowDo;
import io.github.dunwu.javadb.hbase.util.JsonUtil;
import org.apache.hadoop.hbase.client.Put;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hbase Get 测试
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-13
 */
public class HbaseTemplateGetTest {

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
    @DisplayName("put、get 单列数据")
    public void test00() throws IOException {
        long timestamp = System.currentTimeMillis();
        HBASE_TEMPLATE.put(TABLE_NAME, "test-key-0", "f1", "name", "user0");
        ColumnDo columnDo = HBASE_TEMPLATE.getColumn(TABLE_NAME, "test-key-0", "f1", "name");
        Assertions.assertThat(columnDo).isNotNull();
        Assertions.assertThat(columnDo.getColumn()).isEqualTo("name");
        Assertions.assertThat(columnDo.getValue()).isEqualTo("user0");

        HBASE_TEMPLATE.put(TABLE_NAME, "test-key-0", timestamp, "f2", "姓名", "张三");
        ColumnDo columnDo2 = HBASE_TEMPLATE.getColumn(TABLE_NAME, "test-key-0", "f2", "姓名");
        Assertions.assertThat(columnDo2).isNotNull();
        Assertions.assertThat(columnDo2.getColumn()).isEqualTo("姓名");
        Assertions.assertThat(columnDo2.getValue()).isEqualTo("张三");
        Assertions.assertThat(columnDo2.getTimestamp()).isEqualTo(timestamp);

        HBASE_TEMPLATE.delete(TABLE_NAME, "test-key-0");
        columnDo = HBASE_TEMPLATE.getColumn(TABLE_NAME, "test-key-0", "f1", "name");
        Assertions.assertThat(columnDo).isNull();
        columnDo2 = HBASE_TEMPLATE.getColumn(TABLE_NAME, "test-key-0", "f2", "姓名");
        Assertions.assertThat(columnDo2).isNull();
    }

    @Test
    @DisplayName("put、get 多列数据")
    public void test01() throws IOException {

        String row = "test-key-1";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> map1 = new HashMap<>(2);
        map1.put("id", 1);
        map1.put("name", "zhangsan");
        Map<String, Object> map2 = new HashMap<>(2);
        map2.put("编号", 1);
        map2.put("姓名", "张三");

        HBASE_TEMPLATE.put(TABLE_NAME, row, timestamp, "f1", map1);
        HBASE_TEMPLATE.put(TABLE_NAME, row, timestamp, "f2", map2);

        Map<String, ColumnDo> f1ColumnMap = HBASE_TEMPLATE.getColumnMap(TABLE_NAME, row, "f1", "id", "name");
        Assertions.assertThat(f1ColumnMap).isNotEmpty();
        Assertions.assertThat(f1ColumnMap.get("id")).isNotNull();
        Assertions.assertThat(f1ColumnMap.get("id").getValue()).isEqualTo(String.valueOf(1));
        Assertions.assertThat(f1ColumnMap.get("name")).isNotNull();
        Assertions.assertThat(f1ColumnMap.get("name").getValue()).isEqualTo("zhangsan");

        Map<String, ColumnDo> f2ColumnMap = HBASE_TEMPLATE.getColumnMap(TABLE_NAME, row, "f2", "编号", "姓名");
        Assertions.assertThat(f2ColumnMap).isNotEmpty();
        Assertions.assertThat(f2ColumnMap.get("编号")).isNotNull();
        Assertions.assertThat(f2ColumnMap.get("编号").getValue()).isEqualTo(String.valueOf(1));
        Assertions.assertThat(f2ColumnMap.get("姓名")).isNotNull();
        Assertions.assertThat(f2ColumnMap.get("姓名").getValue()).isEqualTo("张三");

        HBASE_TEMPLATE.delete(TABLE_NAME, row);
        f1ColumnMap = HBASE_TEMPLATE.getColumnMap(TABLE_NAME, row, "f1", "id", "name");
        Assertions.assertThat(f1ColumnMap).isEmpty();
        f2ColumnMap = HBASE_TEMPLATE.getColumnMap(TABLE_NAME, row, "f2", "编号", "姓名");
        Assertions.assertThat(f2ColumnMap).isEmpty();
    }

    @Test
    @DisplayName("put、get 列族数据")
    public void test02() throws IOException {

        String row = "test-key-2";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> map1 = new HashMap<>(2);
        map1.put("id", 1);
        map1.put("name", "zhangsan");
        Map<String, Object> map2 = new HashMap<>(2);
        map2.put("编号", 1);
        map2.put("姓名", "张三");

        HBASE_TEMPLATE.put(TABLE_NAME, row, timestamp, "f1", map1);
        HBASE_TEMPLATE.put(TABLE_NAME, row, timestamp, "f2", map2);

        FamilyDo f1 = HBASE_TEMPLATE.getFamily(TABLE_NAME, row, "f1");
        Assertions.assertThat(f1).isNotNull();
        Assertions.assertThat(f1.getColumnMap().get("id")).isNotNull();
        Assertions.assertThat(f1.getColumnMap().get("id").getValue()).isEqualTo(String.valueOf(1));
        Assertions.assertThat(f1.getColumnMap().get("name")).isNotNull();
        Assertions.assertThat(f1.getColumnMap().get("name").getValue()).isEqualTo("zhangsan");

        FamilyDo f2 = HBASE_TEMPLATE.getFamily(TABLE_NAME, row, "f2");
        Assertions.assertThat(f2).isNotNull();
        Assertions.assertThat(f2.getColumnMap().get("编号")).isNotNull();
        Assertions.assertThat(f2.getColumnMap().get("编号").getValue()).isEqualTo(String.valueOf(1));
        Assertions.assertThat(f2.getColumnMap().get("姓名")).isNotNull();
        Assertions.assertThat(f2.getColumnMap().get("姓名").getValue()).isEqualTo("张三");

        HBASE_TEMPLATE.delete(TABLE_NAME, row);
        f1 = HBASE_TEMPLATE.getFamily(TABLE_NAME, row, "f1");
        Assertions.assertThat(f1).isNull();
        f2 = HBASE_TEMPLATE.getFamily(TABLE_NAME, row, "f2");
        Assertions.assertThat(f2).isNull();
    }

    @Test
    @DisplayName("put、get 单行数据")
    public void test03() throws IOException {

        String row = "test-key-3";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> map1 = new HashMap<>(2);
        map1.put("id", 1);
        map1.put("name", "zhangsan");
        Map<String, Object> map2 = new HashMap<>(2);
        map2.put("编号", 1);
        map2.put("姓名", "张三");
        Map<String, Map<String, Object>> familyMap = new HashMap<>(2);
        familyMap.put("f1", map1);
        familyMap.put("f2", map2);

        HBASE_TEMPLATE.put(TABLE_NAME, row, timestamp, familyMap);

        RowDo rowDo = HBASE_TEMPLATE.getRow(TABLE_NAME, row);
        Assertions.assertThat(rowDo).isNotNull();

        FamilyDo f1 = rowDo.getFamilyMap().get("f1");
        Assertions.assertThat(f1).isNotNull();
        Assertions.assertThat(f1.getColumnMap()).isNotEmpty();
        Assertions.assertThat(f1.getColumnMap().get("id")).isNotNull();
        Assertions.assertThat(f1.getColumnMap().get("id").getValue()).isEqualTo(String.valueOf(1));
        Assertions.assertThat(f1.getColumnMap().get("name")).isNotNull();
        Assertions.assertThat(f1.getColumnMap().get("name").getValue()).isEqualTo("zhangsan");

        FamilyDo f2 = rowDo.getFamilyMap().get("f2");
        Assertions.assertThat(f2).isNotNull();
        Assertions.assertThat(f2.getColumnMap()).isNotEmpty();
        Assertions.assertThat(f2.getColumnMap().get("编号")).isNotNull();
        Assertions.assertThat(f2.getColumnMap().get("编号").getValue()).isEqualTo(String.valueOf(1));
        Assertions.assertThat(f2.getColumnMap().get("姓名")).isNotNull();
        Assertions.assertThat(f2.getColumnMap().get("姓名").getValue()).isEqualTo("张三");

        HBASE_TEMPLATE.delete(TABLE_NAME, row);
        rowDo = HBASE_TEMPLATE.getRow(TABLE_NAME, row);
        Assertions.assertThat(rowDo).isNull();
    }

    @Test
    @DisplayName("put get 多行数据")
    public void test04() throws IOException, InterruptedException {

        long timestamp = System.currentTimeMillis();

        Map<String, Object> columnMap1 = new HashMap<>(2);
        columnMap1.put("id", 1);
        columnMap1.put("name", "zhangsan");
        Put put = HbaseTemplate.newPut("test-key-1", timestamp, "f1", columnMap1);

        Map<String, Object> columnMap2 = new HashMap<>(2);
        columnMap2.put("id", 2);
        columnMap2.put("name", "lisi");
        Put put2 = HbaseTemplate.newPut("test-key-2", timestamp, "f1", columnMap2);

        List<Put> puts = CollectionUtil.newArrayList(put, put2);

        HBASE_TEMPLATE.batchPut(TABLE_NAME, puts);

        Map<String, RowDo> rowMap = HBASE_TEMPLATE.getRowMap(TABLE_NAME, "test-key-1", "test-key-2");

        RowDo rowDo1 = rowMap.get("test-key-1");
        Assertions.assertThat(rowDo1).isNotNull();
        FamilyDo f1 = rowDo1.getFamilyMap().get("f1");
        Assertions.assertThat(f1).isNotNull();
        Assertions.assertThat(f1.getColumnMap()).isNotEmpty();
        Assertions.assertThat(f1.getColumnMap().get("id")).isNotNull();
        Assertions.assertThat(f1.getColumnMap().get("id").getValue()).isEqualTo(String.valueOf(1));
        Assertions.assertThat(f1.getColumnMap().get("name")).isNotNull();
        Assertions.assertThat(f1.getColumnMap().get("name").getValue()).isEqualTo("zhangsan");

        RowDo rowDo2 = rowMap.get("test-key-2");
        FamilyDo f2 = rowDo2.getFamilyMap().get("f1");
        Assertions.assertThat(f2).isNotNull();
        Assertions.assertThat(f2.getColumnMap()).isNotEmpty();
        Assertions.assertThat(f2.getColumnMap().get("id")).isNotNull();
        Assertions.assertThat(f2.getColumnMap().get("id").getValue()).isEqualTo(String.valueOf(2));
        Assertions.assertThat(f2.getColumnMap().get("name")).isNotNull();
        Assertions.assertThat(f2.getColumnMap().get("name").getValue()).isEqualTo("lisi");

        HBASE_TEMPLATE.batchDelete(TABLE_NAME, "test-key-1", "test-key-2");
        rowDo1 = HBASE_TEMPLATE.getRow(TABLE_NAME, "test-key-1");
        Assertions.assertThat(rowDo1).isNull();
        rowDo2 = HBASE_TEMPLATE.getRow(TABLE_NAME, "test-key-2");
        Assertions.assertThat(rowDo2).isNull();
    }

    @Test
    @DisplayName("put get 简单 Java 实体数据")
    public void test05() throws IOException, InterruptedException {

        User originUser1 = new User(1, "user1");
        HBASE_TEMPLATE.put(TABLE_NAME, "test-key-1", "f1", originUser1);
        User user1 = HBASE_TEMPLATE.getEntity(TABLE_NAME, "test-key-1", "f1", User.class);
        Assertions.assertThat(user1).isNotNull();
        Assertions.assertThat(ObjectUtil.equals(originUser1, user1)).isTrue();

        HBASE_TEMPLATE.batchDelete(TABLE_NAME, "test-key-1", "test-key-2");
        user1 = HBASE_TEMPLATE.getEntity(TABLE_NAME, "test-key-1", "f1", User.class);
        Assertions.assertThat(user1).isNull();
    }

    @Test
    @DisplayName("put get 实现 BaseHbaseEntity 的简单 Java 实体数据")
    public void test06() throws IOException, InterruptedException {

        Product product1 = new Product("1", "product1", new BigDecimal(4000.0));
        Product product2 = new Product("2", "product2", new BigDecimal(5000.0));
        List<Product> products = CollectionUtil.newArrayList(product1, product2);
        HBASE_TEMPLATE.batchPut(TABLE_NAME, "f1", products);

        List<String> rows = products.stream().map(BaseHbaseEntity::getRowKey).collect(Collectors.toList());
        List<Product> list = HBASE_TEMPLATE.getEntityList(TABLE_NAME, rows, "f1", Product.class);
        Assertions.assertThat(list).isNotEmpty();
        Assertions.assertThat(list.size()).isEqualTo(rows.size());

        HBASE_TEMPLATE.batchDelete(TABLE_NAME, rows.toArray(new String[0]));
        product1 = HBASE_TEMPLATE.getEntity(TABLE_NAME, "test-key-1", "f1", Product.class);
        Assertions.assertThat(product1).isNull();
        product2 = HBASE_TEMPLATE.getEntity(TABLE_NAME, "test-key-2", "f1", Product.class);
        Assertions.assertThat(product2).isNull();
        list = HBASE_TEMPLATE.getEntityList(TABLE_NAME, rows, "f1", Product.class);
        Assertions.assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("put get 实现 BaseHbaseEntity 的复杂 Java 实体数据")
    public void test07() throws IOException {

        Date now = new Date();
        Product product1 = new Product("1", "product1", new BigDecimal(4000.0));
        Product product2 = new Product("2", "product2", new BigDecimal(5000.0));
        List<Product> products = CollectionUtil.newArrayList(product1, product2);
        User user1 = new User(1, "user1");
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("type", "tool");
        tags.put("color", "red");

        Order originOrder = Order.builder()
                                 .id("1")
                                 .user(user1)
                                 .products(products)
                                 .desc("测试订单")
                                 .date(now)
                                 .tags(tags)
                                 .build();

        HBASE_TEMPLATE.put(TABLE_NAME, "f1", originOrder);

        Order order = HBASE_TEMPLATE.getEntity(TABLE_NAME, originOrder.getRowKey(), "f1", Order.class);
        Assertions.assertThat(order).isNotNull();
        Assertions.assertThat(order.getDate()).isNotNull().isEqualTo(now);
        Assertions.assertThat(order.getTags()).isNotNull().isEqualTo(tags);
        Assertions.assertThat(order.getUser()).isNotNull().isEqualTo(user1);
        Assertions.assertThat(order.getProducts()).isNotEmpty();

        System.out.println("order: " + JsonUtil.toString(order));

        HBASE_TEMPLATE.delete(TABLE_NAME, originOrder.getRowKey());
        order = HBASE_TEMPLATE.getEntity(TABLE_NAME, order.getRowKey(), "f1", Order.class);
        Assertions.assertThat(order).isNull();
    }

}

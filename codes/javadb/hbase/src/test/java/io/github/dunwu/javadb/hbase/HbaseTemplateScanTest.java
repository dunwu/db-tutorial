package io.github.dunwu.javadb.hbase;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.dunwu.javadb.hbase.entity.common.PageData;
import io.github.dunwu.javadb.hbase.entity.common.RowDo;
import io.github.dunwu.javadb.hbase.entity.common.ScrollData;
import io.github.dunwu.javadb.hbase.entity.scan.MultiFamilyScan;
import io.github.dunwu.javadb.hbase.entity.scan.SingleFamilyScan;
import org.apache.hadoop.hbase.client.Put;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Get 测试集
 * <p>
 * 测试前，先完整执行 {@link HbaseTemplateGetTest}
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-13
 */
public class HbaseTemplateScanTest {

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
    @DisplayName("批量初始化")
    public void init() throws IOException, InterruptedException {
        List<Product> products = new ArrayList<>();
        List<Put> userPuts = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Product product = new Product(String.valueOf(i), "product" + i,
                new BigDecimal(RandomUtil.randomDouble(9999.0)));
            products.add(product);

            User user = new User(i, "user" + i);
            Put put = HbaseTemplate.newPut(product.getRowKey(), null, "f2", user);
            userPuts.add(put);
        }
        HBASE_TEMPLATE.batchPut(TABLE_NAME, "f1", products);
        HBASE_TEMPLATE.batchPut(TABLE_NAME, userPuts);
    }

    @Test
    @DisplayName("单列族分页查询")
    public void test01() throws IOException {
        SingleFamilyScan scan = new SingleFamilyScan();
        scan.setFamily("f1")
            .setTableName(TABLE_NAME)
            .setPage(1)
            .setSize(10)
            .setReversed(true);
        PageData<RowDo> firstPage = HBASE_TEMPLATE.page(scan);
        System.out.println(StrUtil.format("第 {} 页数据: {}", 1, JSONUtil.toJsonStr(firstPage)));

        int totalPages = firstPage.getTotalPages();
        for (int page = 2; page <= totalPages; page++) {
            scan.setPage(page);
            PageData<RowDo> nextPage = HBASE_TEMPLATE.page(scan);
            System.out.println(StrUtil.format("第 {} 页数据: {}", page, JSONUtil.toJsonStr(nextPage)));
            Assertions.assertThat(nextPage).isNotNull();
        }
    }

    @Test
    @DisplayName("多列族分页查询")
    public void test02() throws IOException {
        Map<String, Collection<String>> familyColumnMap = new HashMap<>();
        familyColumnMap.put("f1", CollectionUtil.newArrayList("id", "name", "price"));
        familyColumnMap.put("f2", CollectionUtil.newArrayList("id", "name"));

        MultiFamilyScan scan = new MultiFamilyScan();
        scan.setFamilyColumnMap(familyColumnMap)
            .setTableName(TABLE_NAME)
            .setPage(1)
            .setSize(10)
            .setReversed(true);
        PageData<RowDo> firstPage = HBASE_TEMPLATE.page(scan);
        System.out.println(StrUtil.format("第 {} 页数据: {}", 1, JSONUtil.toJsonStr(firstPage)));

        int totalPages = firstPage.getTotalPages();
        for (int page = 1; page <= totalPages; page++) {
            scan.setPage(page);
            PageData<RowDo> nextPage = HBASE_TEMPLATE.page(scan);
            System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonStr(nextPage)));
            Assertions.assertThat(nextPage).isNotNull();
        }
    }

    @Test
    @DisplayName("实体分页查询")
    public void test03() throws IOException {

        SingleFamilyScan scan = new SingleFamilyScan();
        scan.setFamily("f2")
            .setTableName(TABLE_NAME)
            .setPage(1)
            .setSize(10)
            .setReversed(true);
        PageData<User> firstPage = HBASE_TEMPLATE.getEntityPage(scan, User.class);
        System.out.println(StrUtil.format("第 {} 页数据: {}", 1, JSONUtil.toJsonStr(firstPage)));

        int totalPages = firstPage.getTotalPages();
        for (int page = 2; page <= totalPages; page++) {
            scan.setPage(page);
            PageData<User> nextPage = HBASE_TEMPLATE.getEntityPage(scan, User.class);
            System.out.println(StrUtil.format("第 {} 页数据: {}", page, JSONUtil.toJsonStr(nextPage)));
            Assertions.assertThat(nextPage).isNotNull();
        }
    }

    @Test
    @DisplayName("单列族滚动查询")
    public void test04() throws IOException {

        SingleFamilyScan scan = new SingleFamilyScan();
        scan.setFamily("f1")
            .setTableName(TABLE_NAME)
            .setSize(10)
            .setReversed(false);

        int page = 1;
        ScrollData<RowDo> first = HBASE_TEMPLATE.scroll(scan);
        System.out.println(StrUtil.format("第 {} 页数据: {}", page, JSONUtil.toJsonPrettyStr(first)));
        Assertions.assertThat(first).isNotNull();
        scan.setScrollRow(first.getScrollRow());

        while (true) {
            page++;
            ScrollData<RowDo> next = HBASE_TEMPLATE.scroll(scan);
            if (next == null || CollectionUtil.isEmpty(next.getContent())) {
                break;
            }
            System.out.println(StrUtil.format("第 {} 页数据: {}", page, JSONUtil.toJsonPrettyStr(first)));
            scan.setScrollRow(next.getScrollRow());
        }
    }

    @Test
    @DisplayName("多列族滚动查询")
    public void test05() throws IOException {
        Map<String, Collection<String>> familyColumnMap = new HashMap<>();
        familyColumnMap.put("f1", CollectionUtil.newArrayList("id", "name", "price"));
        familyColumnMap.put("f2", CollectionUtil.newArrayList("id", "name"));

        MultiFamilyScan scan = new MultiFamilyScan();
        scan.setFamilyColumnMap(familyColumnMap)
            .setTableName(TABLE_NAME)
            .setSize(10)
            .setReversed(true);

        ScrollData<RowDo> first = HBASE_TEMPLATE.scroll(scan);
        System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(first)));
        Assertions.assertThat(first).isNotNull();
        scan.setScrollRow(first.getScrollRow());

        while (true) {
            ScrollData<RowDo> next = HBASE_TEMPLATE.scroll(scan);
            if (next == null || CollectionUtil.isEmpty(next.getContent())) {
                break;
            }
            System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(next)));
            scan.setScrollRow(next.getScrollRow());
        }
    }

    @Test
    @DisplayName("滚动查询实体")
    public void test06() throws IOException {

        SingleFamilyScan scan = new SingleFamilyScan();
        scan.setFamily("f1")
            .setTableName(TABLE_NAME)
            .setSize(10)
            .setReversed(false);

        ScrollData<Product> first = HBASE_TEMPLATE.getEntityScroll(scan, Product.class);
        System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(first)));
        Assertions.assertThat(first).isNotNull();
        scan.setScrollRow(first.getScrollRow());

        while (true) {
            ScrollData<Product> next = HBASE_TEMPLATE.getEntityScroll(scan, Product.class);
            if (next == null || CollectionUtil.isEmpty(next.getContent())) {
                break;
            }
            System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(next)));
            scan.setScrollRow(next.getScrollRow());
        }
    }

    @Test
    @DisplayName("滚动删除全部记录")
    public void clear() throws IOException, InterruptedException {

        SingleFamilyScan scan = new SingleFamilyScan();
        scan.setFamily("f1")
            .setTableName(TABLE_NAME)
            .setSize(100)
            .setReversed(false);

        ScrollData<RowDo> first = HBASE_TEMPLATE.scroll(scan);
        System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(first)));
        Assertions.assertThat(first).isNotNull();
        scan.setScrollRow(first.getScrollRow());
        HBASE_TEMPLATE.batchDelete(TABLE_NAME,
            first.getContent().stream().map(RowDo::getRow).distinct().toArray(String[]::new));

        while (true) {
            ScrollData<RowDo> next = HBASE_TEMPLATE.scroll(scan);
            if (next == null || CollectionUtil.isEmpty(next.getContent())) {
                break;
            }
            System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(next)));
            scan.setScrollRow(next.getScrollRow());
            HBASE_TEMPLATE.batchDelete(TABLE_NAME,
                next.getContent().stream().map(RowDo::getRow).distinct().toArray(String[]::new));
        }
    }

}

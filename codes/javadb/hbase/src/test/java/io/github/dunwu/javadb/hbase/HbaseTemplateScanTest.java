package io.github.dunwu.javadb.hbase;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.dunwu.javadb.hbase.entity.PageData;
import io.github.dunwu.javadb.hbase.entity.RowDo;
import io.github.dunwu.javadb.hbase.entity.ScrollData;
import io.github.dunwu.javadb.hbase.entity.scan.MultiFamilyScan;
import io.github.dunwu.javadb.hbase.entity.scan.SingleFamilyScan;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Get 测试集
 * <p>
 * 测试前，先完整执行 {@link HbaseTemplatePutTest}
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
    @DisplayName("单列族分页查询")
    public void page() throws IOException {
        for (int page = 1; page <= 2; page++) {
            SingleFamilyScan scan = new SingleFamilyScan();
            scan.setFamily("f1")
                .setTableName(TABLE_NAME)
                .setPage(page)
                .setSize(2)
                .setReversed(true);
            PageData<RowDo> rowDoMap = HBASE_TEMPLATE.page(scan);
            System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonStr(rowDoMap)));
            Assertions.assertThat(rowDoMap).isNotNull();
        }
    }

    @Test
    @DisplayName("多列族分页查询")
    public void page2() throws IOException {
        Map<String, Collection<String>> familyColumnMap = new HashMap<>();
        familyColumnMap.put("f1", Collections.singleton("id"));
        familyColumnMap.put("f2", Collections.singleton("name"));
        for (int page = 1; page <= 2; page++) {
            MultiFamilyScan scan = new MultiFamilyScan();
            scan.setFamilyColumnMap(familyColumnMap)
                .setTableName(TABLE_NAME)
                .setPage(page)
                .setSize(2)
                .setReversed(true);
            PageData<RowDo> rowDoMap = HBASE_TEMPLATE.page(scan);
            System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonStr(rowDoMap)));
            Assertions.assertThat(rowDoMap).isNotNull();
        }
    }

    @Test
    @DisplayName("查询实体列表")
    public void getEntityPage() throws IOException {
        SingleFamilyScan scan = new SingleFamilyScan();
        scan.setFamily("f1")
            .setTableName(TABLE_NAME)
            .setPage(1)
            .setSize(2)
            .setReversed(true);
        PageData<User> entityPage = HBASE_TEMPLATE.getEntityPage(scan, User.class);
        System.out.println(StrUtil.format("查询实体列表: {}", JSONUtil.toJsonStr(entityPage)));
        Assertions.assertThat(entityPage).isNotNull();
    }

    @Test
    @DisplayName("单列族滚动查询")
    public void scroll() throws IOException {

        SingleFamilyScan scan = new SingleFamilyScan();
        scan.setFamily("f1")
            .setTableName(TABLE_NAME)
            .setSize(1)
            .setStartRow("test-key-1")
            .setStopRow("test-key-9")
            .setReversed(false);
        ScrollData<RowDo> data = HBASE_TEMPLATE.scroll(scan);
        System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(data)));
        Assertions.assertThat(data).isNotNull();
        scan.setScrollRow(data.getScrollRow());

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
    @DisplayName("多列族滚动查询")
    public void scroll2() throws IOException {
        List<String> userFields = Stream.of(ReflectUtil.getFields(User.class))
                                        .map(Field::getName).collect(Collectors.toList());
        List<String> productFields = Stream.of(ReflectUtil.getFields(Product.class))
                                           .map(Field::getName).collect(Collectors.toList());
        Map<String, Collection<String>> familyColumnMap = new HashMap<>();
        familyColumnMap.put("f1", userFields);
        familyColumnMap.put("f2", productFields);

        MultiFamilyScan scan = new MultiFamilyScan();
        scan.setFamilyColumnMap(familyColumnMap)
            .setTableName(TABLE_NAME)
            .setSize(1)
            .setStartRow("test-key-1")
            .setStopRow("test-key-9")
            .setReversed(true);
        ScrollData<RowDo> data = HBASE_TEMPLATE.scroll(scan);
        System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(data)));
        Assertions.assertThat(data).isNotNull();
        scan.setScrollRow(data.getScrollRow());

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
    public void getEntityScroll() throws IOException {

        SingleFamilyScan scan = new SingleFamilyScan();
        scan.setFamily("f1")
            .setTableName(TABLE_NAME)
            .setSize(1)
            .setStartRow("test-key-1")
            .setStopRow("test-key-9")
            .setReversed(false);

        ScrollData<User> data = HBASE_TEMPLATE.getEntityScroll(scan, User.class);
        System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(data)));
        Assertions.assertThat(data).isNotNull();
        scan.setScrollRow(data.getScrollRow());

        while (true) {
            ScrollData<User> next = HBASE_TEMPLATE.getEntityScroll(scan, User.class);
            if (next == null || CollectionUtil.isEmpty(next.getContent())) {
                break;
            }
            System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonPrettyStr(next)));
            scan.setScrollRow(next.getScrollRow());
        }
    }

}

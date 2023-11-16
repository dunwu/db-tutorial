package io.github.dunwu.javadb.hbase;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.dunwu.javadb.hbase.entity.ColumnDo;
import io.github.dunwu.javadb.hbase.entity.FamilyDo;
import io.github.dunwu.javadb.hbase.entity.RowDo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Get 测试集
 * <p>
 * 测试前，先完整执行 {@link HbaseTemplatePutTest}
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
    @DisplayName("查询实体")
    public void getEntity() throws IOException {
        User user = HBASE_TEMPLATE.getEntity(TABLE_NAME, "test-key-3", "f1", User.class);
        System.out.println(StrUtil.format("查询实体: {}", JSONUtil.toJsonStr(user)));
        Assertions.assertThat(user).isNotNull();
    }

    @Test
    @DisplayName("查询实体列表")
    public void getEntityList() throws IOException {
        List<String> rows = Arrays.asList("test-key-3", "test-key-4", "test-key-5");
        List<User> list = HBASE_TEMPLATE.getEntityList(TABLE_NAME, rows.toArray(new String[0]), "f1", User.class);
        System.out.println(StrUtil.format("查询实体列表: {}", JSONUtil.toJsonStr(list)));
        Assertions.assertThat(list).isNotEmpty();
        Assertions.assertThat(list.size()).isEqualTo(rows.size());
    }

    @Test
    @DisplayName("查询列")
    public void getColumn() throws IOException {
        ColumnDo columnDo = HBASE_TEMPLATE.getColumn(TABLE_NAME, "test-key-1", "f1", "key");
        System.out.println(StrUtil.format("查询单列: {}", JSONUtil.toJsonStr(columnDo)));
    }

    @Test
    @DisplayName("查询多列")
    public void getColumnMap() throws IOException {
        Map<String, ColumnDo> columnMap = HBASE_TEMPLATE.getColumnMap(TABLE_NAME, "test-key-3", "f1");
        System.out.println(StrUtil.format("查询多列: {}", JSONUtil.toJsonStr(columnMap)));
        Assertions.assertThat(columnMap).isNotEmpty();

        Map<String, ColumnDo> columnMap2 = HBASE_TEMPLATE.getColumnMap(TABLE_NAME, "test-key-3", "f1", "id", "name");
        System.out.println(StrUtil.format("查询多列: {}", JSONUtil.toJsonStr(columnMap2)));
        Assertions.assertThat(columnMap2).isNotEmpty();
    }

    @Test
    @DisplayName("查询列族")
    public void getFamily() throws IOException {
        FamilyDo familyDo = HBASE_TEMPLATE.getFamily(TABLE_NAME, "test-key-7", "f1");
        System.out.println(StrUtil.format("查询列族: {}", JSONUtil.toJsonStr(familyDo)));
        Assertions.assertThat(familyDo).isNotNull();
        Assertions.assertThat(familyDo.getFamily()).isEqualTo("f1");

        FamilyDo familyDo2 = HBASE_TEMPLATE.getFamily(TABLE_NAME, "test-key-7", "f2");
        System.out.println(StrUtil.format("查询列族: {}", JSONUtil.toJsonStr(familyDo2)));
        Assertions.assertThat(familyDo2).isNotNull();
        Assertions.assertThat(familyDo2.getFamily()).isEqualTo("f2");
    }

    @Test
    @DisplayName("查询多列族")
    public void getFamilyMap() throws IOException {
        Map<String, Collection<String>> familyColumnMap = new HashMap<>();
        familyColumnMap.put("f1", Collections.singleton("id"));
        familyColumnMap.put("f2", Collections.singleton("name"));
        Map<String, FamilyDo> familyMap = HBASE_TEMPLATE.getFamilyMap(TABLE_NAME, "test-key-7", familyColumnMap);
        System.out.println(StrUtil.format("查询多列族: {}", JSONUtil.toJsonStr(familyMap)));
        Assertions.assertThat(familyMap).isNotEmpty();
        Assertions.assertThat(familyMap.size()).isEqualTo(familyColumnMap.size());
    }

    @Test
    @DisplayName("查询行")
    public void getRow() throws IOException {
        RowDo rowDo = HBASE_TEMPLATE.getRow(TABLE_NAME, "test-key-7");
        System.out.println(StrUtil.format("查询行: {}", JSONUtil.toJsonStr(rowDo)));
        Assertions.assertThat(rowDo).isNotNull();
        Assertions.assertThat(rowDo.getRow()).isEqualTo("test-key-7");
    }

    @Test
    @DisplayName("批量查询行记录")
    public void getRowMap() throws IOException {
        String[] rows = new String[] { "test-key-3", "test-key-4", "test-key-7" };
        Map<String, RowDo> rowMap = HBASE_TEMPLATE.getRowMap(TABLE_NAME, rows);
        System.out.println(StrUtil.format("批量查询行记录: {}", JSONUtil.toJsonStr(rowMap)));
        Assertions.assertThat(rowMap).isNotEmpty();
        Assertions.assertThat(rowMap.size()).isEqualTo(rows.length);
    }

}

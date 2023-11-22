package io.github.dunwu.javadb.hbase.entity.common;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * HBase 列实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnDo {

    /** 表名 */
    private String tableName;
    /** 行 */
    private String row;
    /** 列族 */
    private String family;
    /** 时间戳 */
    private Long timestamp;
    /** 列 */
    private String column;
    /** 列值 */
    private String value;

    public boolean check() {
        return check(this);
    }

    public static boolean check(ColumnDo columnDo) {
        return columnDo != null
            && StrUtil.isNotBlank(columnDo.getTableName())
            && StrUtil.isNotBlank(columnDo.getRow())
            && StrUtil.isNotBlank(columnDo.getFamily())
            && StrUtil.isNotEmpty(columnDo.getColumn());
    }

    public static Map<String, ColumnDo> toColumnMap(String tableName, String row, String family,
        Map<String, String> columnValueMap) {
        if (MapUtil.isEmpty(columnValueMap)) {
            return new HashMap<>(0);
        }
        Map<String, ColumnDo> map = new HashMap<>(columnValueMap.size());
        columnValueMap.forEach((column, value) -> {
            ColumnDo columnDo = new ColumnDo(tableName, row, family, null, column, value);
            if (columnDo.check()) {
                map.put(column, columnDo);
            }
        });
        return map;
    }

    public static Map<String, String> toKvMap(Map<String, ColumnDo> columnMap) {
        if (MapUtil.isEmpty(columnMap)) {
            return new HashMap<>(0);
        }
        Collection<ColumnDo> columns = columnMap.values().stream()
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toList());
        Map<String, String> map = new HashMap<>(columns.size());
        for (ColumnDo columnDo : columns) {
            if (columnDo.check()) {
                map.put(columnDo.getColumn(), columnDo.getValue());
            }
        }
        return map;
    }

}

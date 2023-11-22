package io.github.dunwu.javadb.hbase.entity.common;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HBase 行实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RowDo {

    /** 表名 */
    private String tableName;
    /** 行 */
    private String row;
    /** 列族 Map（key 为 family；value 为列族详细信息） */
    private Map<String, FamilyDo> familyMap;

    public boolean check() {
        return check(this);
    }

    public Map<String, Map<String, String>> getFamilyKvMap() {
        return RowDo.getFamilyKvMap(this);
    }

    public static boolean check(RowDo rowDo) {
        return rowDo != null
            && StrUtil.isNotBlank(rowDo.getTableName())
            && StrUtil.isNotBlank(rowDo.getRow())
            && MapUtil.isNotEmpty(rowDo.getFamilyMap());
    }

    public static Map<String, Map<String, String>> getFamilyKvMap(RowDo rowDo) {
        if (rowDo == null || MapUtil.isEmpty(rowDo.getFamilyMap())) {
            return new HashMap<>(0);
        }
        Map<String, Map<String, String>> kvMap = new HashMap<>(rowDo.getFamilyMap().size());
        rowDo.getFamilyMap().forEach((family, familyDo) -> {
            kvMap.put(family, familyDo.getColumnKvMap());
        });
        return kvMap;
    }

    public static Map<String, RowDo> toRowMap(String tableName, Map<String, Map<String, Map<String, String>>> map) {
        if (MapUtil.isEmpty(map)) {
            return new HashMap<>(0);
        }

        Map<String, RowDo> rowMap = new HashMap<>(map.size());
        map.forEach((row, familyMap) -> {
            RowDo rowDo = new RowDo(tableName, row, FamilyDo.toFamilyMap(tableName, row, familyMap));
            rowMap.put(row, rowDo);
        });
        return rowMap;
    }

    public static List<RowDo> toRowList(String tableName, Map<String, Map<String, Map<String, String>>> map) {
        Map<String, RowDo> rowMap = toRowMap(tableName, map);
        if (MapUtil.isEmpty(rowMap)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(rowMap.values());
    }

    public static RowDo toRow(String tableName, String row, Map<String, Map<String, String>> familyColumnMap) {
        if (MapUtil.isEmpty(familyColumnMap)) {
            return null;
        }
        Map<String, FamilyDo> familyMap = FamilyDo.toFamilyMap(tableName, row, familyColumnMap);
        return new RowDo(tableName, row, familyMap);
    }

}

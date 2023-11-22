package io.github.dunwu.javadb.hbase.entity.common;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * HBase 列族实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDo {

    /** 表名 */
    private String tableName;
    /** 行 */
    private String row;
    /** 列族 */
    private String family;
    /** 列 Map（key 为 column；value 为列详细信息） */
    private Map<String, ColumnDo> columnMap;

    public boolean check() {
        return check(this);
    }

    public Map<String, String> getColumnKvMap() {
        return FamilyDo.getColumnKvMap(this);
    }

    public static Map<String, String> getColumnKvMap(FamilyDo familyDo) {
        if (familyDo == null || MapUtil.isEmpty(familyDo.getColumnMap())) {
            return new HashMap<>(0);
        }
        return ColumnDo.toKvMap(familyDo.getColumnMap());
    }

    public static boolean check(FamilyDo familyDo) {
        return familyDo != null
            && StrUtil.isNotBlank(familyDo.getTableName())
            && StrUtil.isNotBlank(familyDo.getRow())
            && StrUtil.isNotBlank(familyDo.getFamily())
            && MapUtil.isNotEmpty(familyDo.getColumnMap());
    }

    public static Map<String, FamilyDo> toFamilyMap(String tableName, String row,
        Map<String, Map<String, String>> familyColumnValueMap) {
        if (MapUtil.isEmpty(familyColumnValueMap)) {
            return new HashMap<>(0);
        }

        Map<String, FamilyDo> familyMap = new HashMap<>(familyColumnValueMap.size());
        familyColumnValueMap.forEach((family, columnMap) -> {
            familyMap.put(family, toFamily(tableName, row, family, columnMap));
        });
        return familyMap;
    }

    public static FamilyDo toFamily(String tableName, String row, String family, Map<String, String> columnValueMap) {
        Map<String, ColumnDo> columnMap = ColumnDo.toColumnMap(tableName, row, family, columnValueMap);
        return new FamilyDo(tableName, row, family, columnMap);
    }

}

package io.github.dunwu.javadb.hbase.entity;

import cn.hutool.core.map.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HBase 行数据结构
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HBaseRowData {

    private String row;
    private Long timestamp;
    private Map<String, HBaseFamilyData> familyMap = new HashMap<>();

    public Map<String, Map<String, String>> toMap() {
        return toMap(this);
    }

    public static HBaseRowData build(String row, Long timestamp, Map<String, HBaseFamilyData> familyMap) {
        return new HBaseRowData(row, timestamp, familyMap);
    }

    public static HBaseRowData buildByMap(String row, Long timestamp, Map<String, Map<String, String>> map) {
        return new HBaseRowData(row, timestamp, HBaseFamilyData.toFamilyMap(map));
    }

    public static Map<String, Map<String, String>> toMap(HBaseRowData data) {

        if (data == null || MapUtil.isEmpty(data.getFamilyMap())) {
            return new HashMap<>(0);
        }

        Map<String, Map<String, String>> map = new HashMap<>(data.getFamilyMap().size());
        data.getFamilyMap().forEach((family, familyData) -> {
            map.put(family, familyData.getColumnMap());
        });
        return map;
    }

    public static Map<String, HBaseRowData> toRowMap(Map<String, Map<String, Map<String, String>>> rowMultiFamilyMap) {
        if (MapUtil.isEmpty(rowMultiFamilyMap)) {
            return new HashMap<>(0);
        }

        Map<String, HBaseRowData> rowDataMap = new HashMap<>(rowMultiFamilyMap.size());
        rowMultiFamilyMap.forEach((row, familyDataMap) -> {
            Map<String, HBaseFamilyData> familyMap = HBaseFamilyData.toFamilyMap(familyDataMap);
            rowDataMap.put(row, new HBaseRowData(row, null, familyMap));
        });
        return rowDataMap;
    }

    public static List<HBaseRowData> toRowList(Map<String, Map<String, Map<String, String>>> rowMultiFamilyMap) {
        Map<String, HBaseRowData> rowMap = toRowMap(rowMultiFamilyMap);
        if (MapUtil.isEmpty(rowMap)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(rowMap.values());
    }

    public static Map<String, HBaseRowData> toRowMap(String family, Map<String, Map<String, String>> rowColumnMap) {
        if (MapUtil.isEmpty(rowColumnMap)) {
            return new HashMap<>(0);
        }

        Map<String, HBaseRowData> rowDataMap = new HashMap<>(rowColumnMap.size());

        rowColumnMap.forEach((row, columnMap) -> {
            HBaseFamilyData familyData = new HBaseFamilyData(family, columnMap);
            Map<String, HBaseFamilyData> familyMap = new HashMap<>();
            familyMap.put(family, familyData);
            rowDataMap.put(row, new HBaseRowData(row, null, familyMap));
        });
        return rowDataMap;
    }

    public static List<HBaseRowData> toRowList(String family, Map<String, Map<String, String>> rowColumnMap) {
        Map<String, HBaseRowData> rowMap = toRowMap(family, rowColumnMap);
        if (MapUtil.isEmpty(rowMap)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(rowMap.values());
    }

}

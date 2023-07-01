package io.github.dunwu.javadb.hbase.entity;

import cn.hutool.core.map.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * HBase 列族数据结构
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HBaseFamilyData {

    private String family;
    private Map<String, String> columnMap;

    public static Map<String, HBaseFamilyData> toFamilyMap(Map<String, Map<String, String>> map) {
        if (MapUtil.isEmpty(map)) {
            return new HashMap<>(0);
        }

        Map<String, HBaseFamilyData> familyMap = new HashMap<>(map.size());
        map.forEach((family, columnMap) -> {
            familyMap.put(family, new HBaseFamilyData(family, columnMap));
        });
        return familyMap;
    }

}

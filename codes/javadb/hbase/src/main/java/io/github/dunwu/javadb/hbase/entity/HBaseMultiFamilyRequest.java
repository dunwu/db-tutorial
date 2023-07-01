package io.github.dunwu.javadb.hbase.entity;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * HBase 封装请求参数
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
@Accessors(chain = true)
public class HBaseMultiFamilyRequest extends BaseFamilyRequest {

    /**
     * 列族, 列族所包含的列（不可为空）
     */
    private final Map<String, Collection<String>> familyColumns = new HashMap<>();

    public HBaseMultiFamilyRequest addFamilyColumn(String family, Collection<String> columns) {
        this.familyColumns.put(family, columns);
        return this;
    }

    public HBaseMultiFamilyRequest addFamilyColumns(Map<String, Collection<String>> familyColumns) {
        if (MapUtil.isNotEmpty(familyColumns)) {
            this.familyColumns.putAll(familyColumns);
        }
        return this;
    }

    @Override
    public Scan toScan() throws IOException {
        Scan scan = super.toScan();
        if (MapUtil.isNotEmpty(familyColumns)) {
            for (Map.Entry<String, Collection<String>> entry : familyColumns.entrySet()) {
                String family = entry.getKey();
                Collection<String> columns = entry.getValue();
                if (CollectionUtil.isNotEmpty(columns)) {
                    for (String column : columns) {
                        scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
                    }
                }
            }
        }
        return scan;
    }

}

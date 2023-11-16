package io.github.dunwu.javadb.hbase.entity.scan;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * HBase 多列族 scan 封装请求参数
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
public class MultiFamilyScan extends BaseScan {

    /**
     * 列族, 列族所包含的列（不可为空）
     */
    private Map<String, Collection<String>> familyColumnMap = new HashMap<>();
    private String scrollRow;

    public Map<String, Collection<String>> getFamilyColumnMap() {
        return familyColumnMap;
    }

    public MultiFamilyScan setFamilyColumnMap(
        Map<String, Collection<String>> familyColumnMap) {
        this.familyColumnMap = familyColumnMap;
        return this;
    }

    public String getScrollRow() {
        return scrollRow;
    }

    public MultiFamilyScan setScrollRow(String scrollRow) {
        this.scrollRow = scrollRow;
        return this;
    }

    @Override
    public Scan toScan() throws IOException {
        Scan scan = super.toScan();
        if (StrUtil.isNotBlank(scrollRow)) {
            scan.withStartRow(Bytes.toBytes(scrollRow), false);
        }
        if (MapUtil.isNotEmpty(familyColumnMap)) {
            for (Map.Entry<String, Collection<String>> entry : familyColumnMap.entrySet()) {
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

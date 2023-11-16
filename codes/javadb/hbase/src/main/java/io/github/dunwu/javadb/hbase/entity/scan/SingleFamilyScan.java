package io.github.dunwu.javadb.hbase.entity.scan;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * HBase 单列族 scan 封装请求参数
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
@Accessors(chain = true)
public class SingleFamilyScan extends BaseScan {

    private String family;
    private Collection<String> columns = new ArrayList<>();
    private String scrollRow;

    @Override
    public Scan toScan() throws IOException {
        Scan scan = super.toScan();
        if (StrUtil.isNotBlank(scrollRow)) {
            scan.withStartRow(Bytes.toBytes(scrollRow), false);
        }
        if (CollectionUtil.isNotEmpty(this.getColumns())) {
            for (String column : columns) {
                scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            }
        }
        return scan;
    }

    public Map<String, Collection<String>> getFamilyColumnMap() {
        if (StrUtil.isNotBlank(family) && CollectionUtil.isNotEmpty(columns)) {
            Map<String, Collection<String>> familyColumnMap = new HashMap<>(1);
            familyColumnMap.put(family, columns);
            return familyColumnMap;
        }
        return new HashMap<>(0);
    }

}

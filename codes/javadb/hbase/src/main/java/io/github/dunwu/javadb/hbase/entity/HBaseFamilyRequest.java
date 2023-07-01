package io.github.dunwu.javadb.hbase.entity;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * HBase 封装请求参数
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
@Accessors(chain = true)
public class HBaseFamilyRequest extends BaseFamilyRequest {

    private String family;
    private Collection<String> columns = new ArrayList<>();

    @Override
    public Scan toScan() throws IOException {
        Scan scan = super.toScan();
        if (CollectionUtil.isNotEmpty(this.getColumns())) {
            for (String column : columns) {
                scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            }
        }
        return scan;
    }

}

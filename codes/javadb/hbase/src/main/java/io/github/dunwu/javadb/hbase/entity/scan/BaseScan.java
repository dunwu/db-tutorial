package io.github.dunwu.javadb.hbase.entity.scan;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HBase 基本 scan 封装请求参数
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
@Accessors(chain = true)
public class BaseScan {

    /** 表名 */
    protected String tableName;
    /** 起始 row */
    protected String startRow;
    /** 结束 row */
    protected String stopRow;
    /** 起始时间 */
    protected Long minTimeStamp;
    /** 结束时间 */
    protected Long maxTimeStamp;
    /** 是否降序（true: 降序；false：正序） */
    protected boolean reversed = false;
    /** 页号 */
    protected Integer page;
    /** 每页记录数大小 */
    protected Integer size = 100;
    /** 过滤器列表 */
    protected List<Filter> filters = new ArrayList<>();

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    public Scan toScan() throws IOException {
        Scan scan = new Scan();

        // 缓存1000条数据
        scan.setCaching(1000);
        scan.setCacheBlocks(false);
        scan.setReversed(reversed);
        if (StrUtil.isNotBlank(startRow)) {
            if (reversed) {
                scan.withStopRow(Bytes.toBytes(startRow), true);
            } else {
                scan.withStartRow(Bytes.toBytes(startRow), true);
            }
        }
        if (StrUtil.isNotBlank(stopRow)) {
            if (reversed) {
                scan.withStartRow(Bytes.toBytes(stopRow), true);
            } else {
                scan.withStopRow(Bytes.toBytes(stopRow), true);
            }
        }
        if (minTimeStamp != null && maxTimeStamp != null) {
            scan.setTimeRange(minTimeStamp, maxTimeStamp);
        }
        if (size != null) {
            PageFilter pageFilter = new PageFilter(size);
            filters.add(pageFilter);
        }
        FilterList filterList = new FilterList();
        for (Filter filter : filters) {
            filterList.addFilter(filter);
        }
        scan.setFilter(filterList);
        return scan;
    }

}

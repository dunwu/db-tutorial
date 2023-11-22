package io.github.dunwu.javadb.hbase;

import io.github.dunwu.javadb.hbase.mapper.BaseHbaseMapper;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public class OrderMapper extends BaseHbaseMapper<Order> {

    public OrderMapper(HbaseTemplate hbaseTemplate) {
        super(hbaseTemplate);
    }

    @Override
    public String getTableName() {
        return "test";
    }

    @Override
    public String getFamily() {
        return "f1";
    }

    @Override
    public Class<Order> getEntityClass() {
        return Order.class;
    }

}

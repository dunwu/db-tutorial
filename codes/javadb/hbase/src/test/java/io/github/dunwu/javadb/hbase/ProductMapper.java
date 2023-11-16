package io.github.dunwu.javadb.hbase;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public class ProductMapper extends BaseHbaseMapper<Product> {

    public ProductMapper(HbaseTemplate hbaseTemplate, HbaseAdmin hbaseAdmin) {
        super(hbaseTemplate, hbaseAdmin);
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
    public Class<Product> getEntityClass() {
        return Product.class;
    }

}

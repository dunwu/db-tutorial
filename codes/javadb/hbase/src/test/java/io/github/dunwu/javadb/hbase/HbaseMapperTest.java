package io.github.dunwu.javadb.hbase;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public class HbaseMapperTest {

    private static ProductMapper mapper;

    static {
        HbaseTemplate hbaseTemplate = null;
        try {
            hbaseTemplate = HbaseFactory.newHbaseTemplate();
            HbaseAdmin hbaseAdmin = HbaseFactory.newHbaseAdmin();
            mapper = new ProductMapper(hbaseTemplate, hbaseAdmin);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("批量保存、查询、删除测试")
    public void batchSave() throws IOException {
        Product product1 = new Product("test-key-8", "product8", new BigDecimal(4000.0));
        Product product2 = new Product("test-key-9", "product9", new BigDecimal(5000.0));
        List<Product> products = CollectionUtil.newArrayList(product1, product2);
        mapper.batchSave(products);

        List<Product> list = mapper.pojoListByRowKeys(Arrays.asList(product1.getRowKey(), product2.getRowKey()));
        Assertions.assertThat(list).isNotEmpty();
        Assertions.assertThat(list.size()).isEqualTo(2);
        System.out.println(JSONUtil.toJsonStr(list));

        mapper.batchDelete(Arrays.asList(product1.getRowKey(), product2.getRowKey()));

        List<Product> list2 = mapper.pojoListByRowKeys(Arrays.asList(product1.getRowKey(), product2.getRowKey()));
        Assertions.assertThat(list2).isEmpty();
    }

    @Test
    @DisplayName("scroll")
    public void scroll() throws IOException {
        Product product1 = new Product("test-key-8", "product8", new BigDecimal(4000.0));
        Product product2 = new Product("test-key-9", "product9", new BigDecimal(5000.0));
        List<Product> products = CollectionUtil.newArrayList(product1, product2);
        mapper.batchSave(products);

        int size = 1;
        String lastRowKey = null;
        List<Product> list = mapper.scroll(null, size);
        if (CollectionUtil.isNotEmpty(list)) {
            Product last = CollectionUtil.getLast(list);
            System.out.println("entity: " + JSONUtil.toJsonPrettyStr(last));
            lastRowKey = last.getRowKey();
        }

        while (true) {
            List<Product> tempList = mapper.scroll(lastRowKey, size);
            if (CollectionUtil.isEmpty(list)) {
                break;
            }
            Product last = CollectionUtil.getLast(tempList);
            if (last == null) {
                break;
            }
            System.out.println("entity: " + JSONUtil.toJsonPrettyStr(last));
            lastRowKey = last.getRowKey();
            if (StrUtil.isBlank(lastRowKey)) {
                break;
            }
        }
    }

}

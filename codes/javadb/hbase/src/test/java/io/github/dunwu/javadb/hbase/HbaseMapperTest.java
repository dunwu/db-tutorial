package io.github.dunwu.javadb.hbase;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
    @DisplayName("batchSave")
    public void batchSave() {
        Product product1 = new Product("test-key-8", "product8", new BigDecimal(4000.0));
        Product product2 = new Product("test-key-9", "product9", new BigDecimal(5000.0));
        List<Product> products = CollectionUtil.newArrayList(product1, product2);
        mapper.batchSave(products);
    }

    @Test
    @DisplayName("batchGet")
    public void batchGet() {
        List<Product> list = mapper.pojoListByIds(Arrays.asList("test-key-8", "test-key-9"));
        System.out.println(JSONUtil.toJsonStr(list));
    }

    @Test
    @DisplayName("scroll")
    public void scroll() {
        int size = 1;
        String lastId = null;
        List<Product> list = mapper.scroll(null, size);
        if (CollectionUtil.isNotEmpty(list)) {
            Product last = CollectionUtil.getLast(list);
            System.out.println("entity: " + JSONUtil.toJsonPrettyStr(last));
            lastId = last.getId();
        }

        while (true) {
            List<Product> products = mapper.scroll(lastId, size);
            if (CollectionUtil.isEmpty(list)) {
                break;
            }
            Product last = CollectionUtil.getLast(products);
            System.out.println("entity: " + JSONUtil.toJsonPrettyStr(last));
            lastId = last.getId();
            if (StrUtil.isBlank(lastId)) {
                break;
            }
        }
    }

}

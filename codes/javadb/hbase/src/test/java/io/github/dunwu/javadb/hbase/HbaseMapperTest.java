package io.github.dunwu.javadb.hbase;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public class HbaseMapperTest {

    private static final OrderMapper mapper;

    static {
        HbaseTemplate hbaseTemplate = null;
        try {
            hbaseTemplate = HbaseFactory.newHbaseTemplate();
            mapper = new OrderMapper(hbaseTemplate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("批量保存、查询、删除 BaseHbaseEntity 实体")
    public void batchSave() {

        Date now = new Date();
        Product product1 = new Product("1", "product1", new BigDecimal(4000.0));
        Product product2 = new Product("2", "product2", new BigDecimal(5000.0));
        List<Product> products = CollectionUtil.newArrayList(product1, product2);
        User user1 = new User(1, "user1");
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("type", "tool");
        tags.put("color", "red");

        Order originOrder = Order.builder()
                                 .id("1")
                                 .user(user1)
                                 .products(products)
                                 .desc("测试订单")
                                 .date(now)
                                 .tags(tags)
                                 .build();
        mapper.batchSave(Collections.singleton(originOrder));

        List<Order> list = mapper.getListByIds(Collections.singleton(originOrder.getRowKey()));
        Assertions.assertThat(list).isNotEmpty();
        Order order = list.get(0);
        Assertions.assertThat(order).isNotNull();
        Assertions.assertThat(order.getDate()).isNotNull().isEqualTo(now);
        Assertions.assertThat(order.getTags()).isNotNull().isEqualTo(tags);
        Assertions.assertThat(order.getUser()).isNotNull().isEqualTo(user1);
        Assertions.assertThat(order.getProducts()).isNotEmpty();
        Assertions.assertThat(list).isNotEmpty();
        Assertions.assertThat(list.size()).isEqualTo(1);
        System.out.println(JSONUtil.toJsonStr(list));

        mapper.deleteBatchIds(Collections.singletonList(originOrder.getRowKey()));

        List<Order> list2 = mapper.getListByIds(Collections.singletonList(originOrder.getRowKey()));
        Assertions.assertThat(list2).isEmpty();
    }

}

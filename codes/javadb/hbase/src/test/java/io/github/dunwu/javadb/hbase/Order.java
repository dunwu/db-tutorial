package io.github.dunwu.javadb.hbase;

import io.github.dunwu.javadb.hbase.annotation.RowKeyRule;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 较为复杂的 Java 实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RowKeyRule(pk = "id", length = 20)
public class Order implements BaseHbaseEntity {

    private String id;
    private User user;
    private List<Product> products;
    private String desc;
    private Date date;
    private Map<String, String> tags;

}

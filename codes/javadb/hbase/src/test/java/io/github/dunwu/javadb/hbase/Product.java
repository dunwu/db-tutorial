package io.github.dunwu.javadb.hbase;

import io.github.dunwu.javadb.hbase.annotation.RowKeyRule;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 产品实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RowKeyRule(uk = "getId", length = 10)
public class Product implements BaseHbaseEntity {

    private String id;
    private String name;
    private BigDecimal price;

    private static final long serialVersionUID = -2596114168690429555L;

}

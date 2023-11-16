package io.github.dunwu.javadb.hbase;

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
public class Product extends BaseHbaseEntity {

    private String id;
    private String name;
    private BigDecimal price;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getIdKey() {
        return "id";
    }

    private static final long serialVersionUID = -2596114168690429555L;

}

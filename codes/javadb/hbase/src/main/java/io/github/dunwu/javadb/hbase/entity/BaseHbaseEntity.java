package io.github.dunwu.javadb.hbase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.dunwu.javadb.hbase.annotation.RowKeyUtil;

/**
 * HBase 基础实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public interface BaseHbaseEntity {

    /**
     * 获取主键
     */
    @JsonIgnore
    default String getRowKey() {
        return RowKeyUtil.getRowKey(this);
    }

}

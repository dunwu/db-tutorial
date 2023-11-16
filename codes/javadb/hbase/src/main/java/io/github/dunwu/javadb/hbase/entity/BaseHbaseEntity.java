package io.github.dunwu.javadb.hbase.entity;

import java.io.Serializable;

/**
 * HBase 基础实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public abstract class BaseHbaseEntity implements Serializable {

    /**
     * 获取主键
     */
    public abstract String getId();

    /**
     * 获取主键字段名
     */
    public abstract String getIdKey();

    private static final long serialVersionUID = 5075127328254616085L;

}

package io.github.dunwu.javadb.elasticsearch.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * ES 实体接口
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2023-06-28
 */
@Data
@ToString
public abstract class BaseEsEntity implements Serializable {

    /**
     * 获取版本
     */
    protected Long version;

    protected Float hitScore;

    public abstract String getDocId();

}

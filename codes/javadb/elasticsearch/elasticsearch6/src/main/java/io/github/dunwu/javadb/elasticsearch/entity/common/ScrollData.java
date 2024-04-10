package io.github.dunwu.javadb.elasticsearch.entity.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;

/**
 * Hbase 滚动数据实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScrollData<T> implements Serializable {

    private String scrollId;
    private int size = 10;
    private long total = 0L;
    private Collection<T> content;

    private static final long serialVersionUID = 1L;

}

package io.github.dunwu.javadb.hbase.entity.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * Hbase 滚动数据实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrollData<T> {

    private String startRow;
    private String stopRow;
    private String scrollRow;
    private Integer size;
    private Collection<T> content;

}

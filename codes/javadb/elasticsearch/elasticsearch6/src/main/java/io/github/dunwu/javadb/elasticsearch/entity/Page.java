package io.github.dunwu.javadb.elasticsearch.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 分页实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-28
 */
@Data
public class Page<T> {

    private long total;
    private int page;
    private int size;
    private List<T> content = new ArrayList<>();

    public Page(long total, int page, int size) {
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public Page(long total, int page, int size, Collection<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.content.addAll(list);
    }

}

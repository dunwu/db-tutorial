package io.github.dunwu.javadb.hbase.entity;

import lombok.Data;

import java.util.Collection;

/**
 * HBase 分页数据实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-05-19
 */
@Data
public class PageData<T> {

    private Integer page;
    private Integer size;
    private Long total;
    private Integer totalPages;
    private Collection<T> content;

    public PageData() { }

    public PageData(Integer page, Integer size, Long total, Collection<T> content) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.content = content;
    }

    public int getTotalPages() {
        return this.getSize() == 0 ? 0 : (int) Math.ceil((double) this.total / (double) this.getSize());
    }

}

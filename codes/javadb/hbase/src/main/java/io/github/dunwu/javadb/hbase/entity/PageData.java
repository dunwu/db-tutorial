package io.github.dunwu.javadb.hbase.entity;

import lombok.Data;

import java.util.Collection;

@Data
public class PageData<T> {

    private Integer number;
    private Integer size;
    private Long total;
    private Integer totalPages;
    private Collection<T> content;

    public PageData() { }

    public PageData(Integer number, Integer size, Long total, Collection<T> content) {
        this.number = number;
        this.size = size;
        this.total = total;
        this.content = content;
    }

    public int getTotalPages() {
        return this.getSize() == 0 ? 0 : (int) Math.ceil((double) this.total / (double) this.getSize());
    }

}

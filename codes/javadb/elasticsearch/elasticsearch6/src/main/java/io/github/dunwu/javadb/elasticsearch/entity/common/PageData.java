package io.github.dunwu.javadb.elasticsearch.entity.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageData<T> implements Serializable {

    private int page;
    private int size;
    private long total;
    private List<T> content = new ArrayList<>();

    public PageData(int page, int size, long total) {
        this.total = total;
        this.page = page;
        this.size = size;
    }

    private static final long serialVersionUID = 1L;

}

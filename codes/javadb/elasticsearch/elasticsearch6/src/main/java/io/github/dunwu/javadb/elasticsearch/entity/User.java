package io.github.dunwu.javadb.elasticsearch.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 用户实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2023-06-28
 */
@Data
@Builder
public class User implements EsEntity {

    private Long id;
    private String username;
    private String password;
    private Integer age;
    private String email;

    @Override
    public String getDocId() {
        return null;
    }

}

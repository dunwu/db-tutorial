package io.github.dunwu.javadb.elasticsearch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2023-06-28
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEsEntity {

    private Long id;
    private String username;
    private String password;
    private Integer age;
    private String email;

    @Override
    public String getDocId() {
        return String.valueOf(id);
    }

}

package io.github.dunwu.javadb.elasticsearch.mapper;

import io.github.dunwu.javadb.elasticsearch.entity.User;

/**
 * User ES Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
public class UserEsMapper extends BaseEsMapper<User> {

    @Override
    public String getIndexAlias() {
        return "user";
    }

    @Override
    public String getIndexName() {
        return "user";
    }

    @Override
    public String getIndexType() {
        return "_doc";
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

}

package io.github.dunwu.javadb.elasticsearch.mapper;

import io.github.dunwu.javadb.elasticsearch.ElasticsearchTemplate;
import io.github.dunwu.javadb.elasticsearch.entity.User;
import org.springframework.stereotype.Component;

/**
 * User ES Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
@Component
public class UserEsMapper extends BaseEsMapper<User> {

    public UserEsMapper(ElasticsearchTemplate elasticsearchTemplate) {
        super(elasticsearchTemplate);
    }

    @Override
    public String getIndex() {
        return "user";
    }

    @Override
    public String getType() {
        return "_doc";
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

}

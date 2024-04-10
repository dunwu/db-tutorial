package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import io.github.dunwu.javadb.elasticsearch.ElasticsearchTemplate;
import io.github.dunwu.javadb.elasticsearch.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * open_applet_consume_yyyyMMdd ES Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
@Slf4j
@Component
public class UserEsMapper extends BaseDynamicEsMapper<User> {

    public UserEsMapper(ElasticsearchTemplate elasticsearchTemplate) {
        super(elasticsearchTemplate);
    }

    @Override
    public String getAlias() {
        return "user";
    }

    @Override
    public String getIndex() {
        String date = DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);
        return getAlias() + "_" + date;
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

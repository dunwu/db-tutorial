package io.github.dunwu.javadb.elasticsearch.config;

import io.github.dunwu.javadb.elasticsearch.ElasticsearchFactory;
import io.github.dunwu.javadb.elasticsearch.ElasticsearchTemplate;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * ES 配置
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2024-02-07
 */
@Configuration
@ComponentScan(value = "io.github.dunwu.javadb.elasticsearch.mapper")
public class ElasticsearchConfig {

    @Bean("restHighLevelClient")
    public RestHighLevelClient restHighLevelClient() {
        return ElasticsearchFactory.newRestHighLevelClient();
    }

    @Bean("elasticsearchTemplate")
    public ElasticsearchTemplate elasticsearchTemplate() {
        return ElasticsearchFactory.newElasticsearchTemplate();
    }

}

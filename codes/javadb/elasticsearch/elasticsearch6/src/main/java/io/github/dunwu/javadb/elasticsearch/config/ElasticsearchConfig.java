package io.github.dunwu.javadb.elasticsearch.config;

import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.elasticsearch.ElasticsearchFactory;
import io.github.dunwu.javadb.elasticsearch.ElasticsearchTemplate;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ES 配置
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2024-02-07
 */
@Configuration
@ComponentScan(value = "io.github.dunwu.javadb.elasticsearch.mapper")
public class ElasticsearchConfig {

    @Value("${es.hosts:#{null}}")
    private String hostsConfig;

    @Bean("restHighLevelClient")
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient() {
        if (hostsConfig == null) {
            return ElasticsearchFactory.newRestHighLevelClient();
        } else {
            List<String> hosts = StrUtil.split(hostsConfig, ",");
            return ElasticsearchFactory.newRestHighLevelClient(hosts);
        }
    }

    @Bean("elasticsearchTemplate")
    public ElasticsearchTemplate elasticsearchTemplate(RestHighLevelClient restHighLevelClient) {
        return ElasticsearchFactory.newElasticsearchTemplate(restHighLevelClient);
    }

}

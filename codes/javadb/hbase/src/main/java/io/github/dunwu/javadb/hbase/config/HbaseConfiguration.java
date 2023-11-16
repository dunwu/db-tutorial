package io.github.dunwu.javadb.hbase.config;

import io.github.dunwu.javadb.hbase.HbaseAdmin;
import io.github.dunwu.javadb.hbase.HbaseFactory;
import io.github.dunwu.javadb.hbase.HbaseTemplate;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * HBase 启动配置
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-07-04
 */
@org.springframework.context.annotation.Configuration
public class HbaseConfiguration {

    @Bean("hbaseTemplate")
    public HbaseTemplate hbaseTemplate() throws IOException {
        return HbaseFactory.newHbaseTemplate();
    }

    @Bean("hbaseAdmin")
    public HbaseAdmin hbaseAdmin() throws IOException {
        return HbaseFactory.newHbaseAdmin();
    }

}

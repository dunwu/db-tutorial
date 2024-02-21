package io.github.dunwu.javadb.elasticsearch;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 客户端实例工厂
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2024-02-07
 */
@Slf4j
public class ElasticsearchFactory {

    public static int CONNECT_TIMEOUT_MILLIS = 1000;

    public static int SOCKET_TIMEOUT_MILLIS = 30000;

    public static int CONNECTION_REQUEST_TIMEOUT_MILLIS = 500;

    public static int MAX_CONN_TOTAL = 30;

    public static int MAX_CONN_PER_ROUTE = 10;

    public static RestClient newRestClient() {
        // 从配置中心读取环境变量
        String env = "test";
        return newRestClient(env);
    }

    public static RestClient newRestClient(String env) {
        String hosts = getDefaultEsAddress(env);
        return newRestClient(toHttpHostList(hosts));
    }

    public static RestClient newRestClient(HttpHost[] httpHosts) {
        RestClientBuilder builder = getRestClientBuilder(httpHosts);
        if (builder == null) {
            return null;
        }
        try {
            return builder.build();
        } catch (Exception e) {
            log.error("【ES】connect failed.", e);
            return null;
        }
    }

    public static RestHighLevelClient newRestHighLevelClient() {
        // 从配置中心读取环境变量
        String env = "test";
        return newRestHighLevelClient(env);
    }

    public static RestHighLevelClient newRestHighLevelClient(String env) {
        String hosts = getDefaultEsAddress(env);
        return newRestHighLevelClient(toHttpHostList(hosts));
    }

    public static RestHighLevelClient newRestHighLevelClient(HttpHost[] httpHosts) {
        RestClientBuilder builder = getRestClientBuilder(httpHosts);
        if (builder == null) {
            return null;
        }
        try {
            return new RestHighLevelClient(builder);
        } catch (Exception e) {
            log.error("【ES】connect failed.", e);
            return null;
        }
    }

    public static ElasticsearchTemplate newElasticsearchTemplate() {
        // 从配置中心读取环境变量
        String env = "test";
        return newElasticsearchTemplate(env);
    }

    public static ElasticsearchTemplate newElasticsearchTemplate(String env) {
        String hosts = getDefaultEsAddress(env);
        return newElasticsearchTemplate(toHttpHostList(hosts));
    }

    public static ElasticsearchTemplate newElasticsearchTemplate(HttpHost[] httpHosts) {
        RestHighLevelClient client = newRestHighLevelClient(httpHosts);
        if (client == null) {
            return null;
        }
        return new ElasticsearchTemplate(client);
    }

    public static ElasticsearchTemplate newElasticsearchTemplate(RestHighLevelClient client) {
        if (client == null) {
            return null;
        }
        return new ElasticsearchTemplate(client);
    }

    public static RestClientBuilder getRestClientBuilder(HttpHost[] httpHosts) {
        if (ArrayUtil.isEmpty(httpHosts)) {
            log.error("【ES】connect failed. hosts are empty.");
            return null;
        }
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);
        restClientBuilder.setRequestConfigCallback(builder -> {
            builder.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            builder.setSocketTimeout(SOCKET_TIMEOUT_MILLIS);
            builder.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLIS);
            return builder;
        });
        restClientBuilder.setHttpClientConfigCallback(builder -> {
            builder.setMaxConnTotal(MAX_CONN_TOTAL);
            builder.setMaxConnPerRoute(MAX_CONN_PER_ROUTE);
            return builder;
        });
        return restClientBuilder;
    }

    private static HttpHost[] toHttpHostList(String hosts) {
        if (StrUtil.isBlank(hosts)) {
            return null;
        }
        List<String> strList = StrUtil.split(hosts, ",");
        List<HttpHost> list = strList.stream().map(str -> {
            List<String> params = StrUtil.split(str, ":");
            return new HttpHost(params.get(0), Integer.parseInt(params.get(1)), "http");
        }).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(list)) {
            return new HttpHost[0];
        }
        return list.toArray(new HttpHost[0]);
    }

    public static String getDefaultEsAddress() {
        // 从配置中心读取环境变量
        String env = "test";
        return getDefaultEsAddress(env);
    }

    private static String getDefaultEsAddress(String env) {
        String defaultAddress;
        switch (env) {
            case "prd":
                defaultAddress = "127.0.0.1:9200,127.0.0.2:9200,127.0.0.3:9200";
                break;
            case "pre":
                defaultAddress = "127.0.0.1:9200";
                break;
            case "test":
            default:
                defaultAddress = "127.0.0.1:9200";
                break;
        }
        return defaultAddress;
    }

}

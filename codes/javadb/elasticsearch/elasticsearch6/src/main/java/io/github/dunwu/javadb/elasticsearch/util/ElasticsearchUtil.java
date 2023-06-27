package io.github.dunwu.javadb.elasticsearch.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ES 工具类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
@Slf4j
public class ElasticsearchUtil {

    public static int CONNECT_TIMEOUT_MILLIS = 1000;
    public static int SOCKET_TIMEOUT_MILLIS = 30000;
    public static int CONNECTION_REQUEST_TIMEOUT_MILLIS = 500;
    public static int MAX_CONN_TOTAL = 30;
    public static int MAX_CONN_PER_ROUTE = 10;

    public static RestClient newRestClient(String hosts) {
        HttpHost[] httpHosts = toHttpHostList(hosts);
        RestClientBuilder builder = builder(httpHosts);
        try {
            return builder.build();
        } catch (Exception e) {
            log.error("【ES】connect failed.", e);
            return null;
        }
    }

    public static RestHighLevelClient newRestHighLevelClient(String hosts) {
        HttpHost[] httpHosts = toHttpHostList(hosts);
        RestClientBuilder builder = builder(httpHosts);
        try {
            return new RestHighLevelClient(builder);
        } catch (Exception e) {
            log.error("【ES】connect failed.", e);
            return null;
        }
    }

    public static HttpHost[] toHttpHostList(String hosts) {
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

    public static RestClientBuilder builder(HttpHost[] httpHosts) {
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

    public static <T> T toPojo(GetResponse response, Class<T> clazz) {
        if (null == response) {
            return null;
        } else if (StrUtil.isBlank(response.getSourceAsString())) {
            return null;
        } else {
            return JSONUtil.toBean(response.getSourceAsString(), clazz);
        }
    }

    public static <T> List<T> toPojoList(SearchResponse response, Class<T> clazz) {
        List<T> list = null;
        try {
            if (response != null) {
                SearchHit[] searchHits = response.getHits().getHits();
                T entity;
                list = new ArrayList<T>(searchHits.length);
                for (SearchHit hit : searchHits) {
                    if (null == hit) {
                        continue;
                    }
                    entity = JSONUtil.toBean(hit.getSourceAsString(), clazz);
                    list.add(entity);
                }
            }
        } catch (Exception e) {
            log.error("解析ES返回结果异常, response:{}", response);
            throw e;
        }
        return list;
    }

    public static <T> Map<String, Object> toMap(T entity) {
        Map<String, Object> map = new HashMap<>();
        BeanUtil.beanToMap(entity, map, CopyOptions.create().ignoreError());
        return map;
    }

}

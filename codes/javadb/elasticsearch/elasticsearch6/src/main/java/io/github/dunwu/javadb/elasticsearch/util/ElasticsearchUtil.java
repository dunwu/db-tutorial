package io.github.dunwu.javadb.elasticsearch.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.dunwu.javadb.elasticsearch.entity.EsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static BulkProcessor newAsyncBulkProcessor(RestHighLevelClient client) {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                if (response.hasFailures()) {
                    log.error("Bulk [{}] executed with failures,response = {}", executionId,
                        response.buildFailureMessage());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            }
        };
        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
            (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
        BulkProcessor bulkProcessor = BulkProcessor.builder(bulkConsumer, listener)
                                                   // 1000条数据请求执行一次bulk
                                                   .setBulkActions(1000)
                                                   // 5mb的数据刷新一次bulk
                                                   .setBulkSize(new ByteSizeValue(5L, ByteSizeUnit.MB))
                                                   // 并发请求数量, 0不并发, 1并发允许执行
                                                   .setConcurrentRequests(2)
                                                   // 固定1s必须刷新一次
                                                   .setFlushInterval(TimeValue.timeValueMillis(1000L))
                                                   // 重试3次，间隔100ms
                                                   .setBackoffPolicy(
                                                       BackoffPolicy.constantBackoff(TimeValue.timeValueMillis(200L),
                                                           3)).build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                bulkProcessor.flush();
                bulkProcessor.awaitClose(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Failed to close bulkProcessor", e);
            }
            log.info("bulkProcessor closed!");
        }));
        return bulkProcessor;
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

    public static <T extends EsEntity> String insert(RestHighLevelClient client, String index, String type, T entity)
        throws IOException {
        Map<String, Object> map = toMap(entity);
        IndexRequest request = new IndexRequest(index, type).source(map);
        if (entity.getDocId() != null) {
            request.id(entity.getDocId());
        }

        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        if (response != null && response.getResult() == DocWriteResponse.Result.CREATED) {
            return response.getId();
        } else {
            return null;
        }
    }

    public static <T extends EsEntity> boolean batchInsert(RestHighLevelClient client, String index, String type,
        Collection<T> list) throws IOException {

        if (CollectionUtil.isEmpty(list)) {
            return true;
        }

        BulkRequest bulkRequest = new BulkRequest();
        for (T entity : list) {
            Map<String, Object> map = ElasticsearchUtil.toMap(entity);
            IndexRequest request = new IndexRequest(index, type).source(map);
            if (entity.getDocId() != null) {
                request.id(entity.getDocId());
            }
            bulkRequest.add(request);
        }

        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return response != null && !response.hasFailures();
    }

    public static <T extends EsEntity> void asyncBatchInsert(RestHighLevelClient client, String index, String type,
        Collection<T> list, ActionListener<BulkResponse> listener) {

        if (CollectionUtil.isEmpty(list)) {
            return;
        }

        BulkRequest bulkRequest = new BulkRequest();
        for (T entity : list) {
            Map<String, Object> map = ElasticsearchUtil.toMap(entity);
            IndexRequest request = new IndexRequest(index, type).source(map);
            if (entity.getDocId() != null) {
                request.id(entity.getDocId());
            }
            bulkRequest.add(request);
        }

        client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener);
    }

    public static <T extends EsEntity> boolean updateById(RestHighLevelClient client, String index, String type,
        T entity) throws IOException {

        if (entity == null || entity.getDocId() == null) {
            return false;
        }

        Map<String, Object> map = toMap(entity);
        UpdateRequest request = new UpdateRequest(index, type, entity.getDocId()).doc(map);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        return response != null && response.getResult() == DocWriteResponse.Result.UPDATED;
    }

    public static <T extends EsEntity> boolean batchUpdateById(RestHighLevelClient client, String index, String type,
        Collection<T> list) throws IOException {

        if (CollectionUtil.isEmpty(list)) {
            return true;
        }

        BulkRequest bulkRequest = new BulkRequest();
        for (T entity : list) {
            if (entity == null || entity.getDocId() == null) {
                continue;
            }
            Map<String, Object> map = ElasticsearchUtil.toMap(entity);
            UpdateRequest request = new UpdateRequest(index, type, entity.getDocId()).doc(map);
            bulkRequest.add(request);
        }

        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return response != null && !response.hasFailures();
    }

    public static <T extends EsEntity> void asyncBatchUpdateById(RestHighLevelClient client, String index,
        String type, Collection<T> list, ActionListener<BulkResponse> listener) {

        if (CollectionUtil.isEmpty(list)) {
            return;
        }

        BulkRequest bulkRequest = new BulkRequest();
        for (T entity : list) {
            if (entity == null || entity.getDocId() == null) {
                continue;
            }
            Map<String, Object> map = ElasticsearchUtil.toMap(entity);
            UpdateRequest request = new UpdateRequest(index, type, entity.getDocId()).doc(map);
            bulkRequest.add(request);
        }

        client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener);
    }

    public static boolean deleteById(RestHighLevelClient client, String index, String type, String id)
        throws IOException {
        return batchDeleteById(client, index, type, Collections.singleton(id));
    }

    public static boolean batchDeleteById(RestHighLevelClient client, String index, String type, Collection<String> ids)
        throws IOException {

        if (CollectionUtil.isEmpty(ids)) {
            return true;
        }

        BulkRequest bulkRequest = new BulkRequest();
        ids.forEach(id -> {
            DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
            bulkRequest.add(deleteRequest);
        });

        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return response != null && !response.hasFailures();
    }

    public static void asyncBatchDeleteById(RestHighLevelClient client, String index, String type,
        Collection<String> ids, ActionListener<BulkResponse> listener) {

        if (CollectionUtil.isEmpty(ids)) {
            return;
        }

        BulkRequest bulkRequest = new BulkRequest();
        ids.forEach(id -> {
            DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
            bulkRequest.add(deleteRequest);
        });

        client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener);
    }

    public static SearchResponse getById(RestHighLevelClient client, String index, String type, String id)
        throws IOException {
        SearchRequest searchRequest = Requests.searchRequest(index).types(type);
        QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds(id);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        searchRequest.source(sourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    public static <T> T pojoById(RestHighLevelClient client, String index, String type, String id, Class<T> clazz)
        throws IOException {
        SearchResponse response = getById(client, index, type, id);
        if (response == null) {
            return null;
        }
        List<T> list = ElasticsearchUtil.toPojoList(response, clazz);
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public static <T> List<T> pojoListByIds(RestHighLevelClient client, String index, String type,
        Collection<String> ids, Class<T> clazz) throws IOException {

        if (CollectionUtil.isEmpty(ids)) {
            return null;
        }

        MultiGetRequest request = new MultiGetRequest();
        for (String id : ids) {
            request.add(new MultiGetRequest.Item(index, type, id));
        }

        MultiGetResponse multiGetResponse = client.mget(request, RequestOptions.DEFAULT);
        if (null == multiGetResponse
            || multiGetResponse.getResponses() == null
            || multiGetResponse.getResponses().length <= 0) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();
        for (MultiGetItemResponse itemResponse : multiGetResponse.getResponses()) {
            if (itemResponse.isFailed()) {
                log.error("通过id获取文档失败", itemResponse.getFailure().getFailure());
            } else {
                T entity = ElasticsearchUtil.toPojo(itemResponse.getResponse(), clazz);
                if (entity != null) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    public static <T> Page<T> pojoPage(RestHighLevelClient client, String index, String type,
        SearchSourceBuilder builder, Class<T> clazz) throws IOException {
        SearchResponse response = query(client, index, type, builder);
        if (response == null || response.status() != RestStatus.OK) {
            return null;
        }

        List<T> content = toPojoList(response, clazz);
        SearchHits searchHits = response.getHits();
        return new Page<>(searchHits.getTotalHits(), builder.from(), builder.size(), content);
    }

    public static SearchResponse query(RestHighLevelClient client, String index, String type,
        SearchSourceBuilder builder) throws IOException {
        SearchRequest request = new SearchRequest(index).types(type);
        request.source(builder);
        return client.search(request, RequestOptions.DEFAULT);
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

        if (response == null || response.status() != RestStatus.OK) {
            return new ArrayList<>();
        }

        if (ArrayUtil.isEmpty(response.getHits().getHits())) {
            return new ArrayList<>();
        }

        return Stream.of(response.getHits().getHits())
                     .map(hit -> JSONUtil.toBean(hit.getSourceAsString(), clazz))
                     .collect(Collectors.toList());
    }

    public static <T> Map<String, Object> toMap(T entity) {
        return JsonUtil.toMap(JsonUtil.toJson(entity));
    }

}

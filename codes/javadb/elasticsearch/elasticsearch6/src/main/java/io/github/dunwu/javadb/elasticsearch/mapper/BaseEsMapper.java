package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import io.github.dunwu.javadb.elasticsearch.entity.EsEntity;
import io.github.dunwu.javadb.elasticsearch.util.ElasticsearchUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * ES Mapper 基础类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
@Slf4j
public abstract class BaseEsMapper<T extends EsEntity> implements EsMapper<T>, Closeable {

    public static final String HOSTS = "127.0.0.1:9200";

    private BulkProcessor bulkProcessor;

    private final RestHighLevelClient restHighLevelClient = ElasticsearchUtil.newRestHighLevelClient(HOSTS);

    @Override
    public RestHighLevelClient getClient() throws IOException {
        Assert.notNull(restHighLevelClient, () -> new IOException("【ES】not connected."));
        return restHighLevelClient;
    }


    @Override
    public synchronized BulkProcessor getBulkProcessor() {
        if (bulkProcessor == null) {
            bulkProcessor = newAsyncBulkProcessor();
        }
        return bulkProcessor;
    }

    @Override
    public boolean isIndexExists() throws IOException {
        IndicesClient indicesClient = getClient().indices();
        GetIndexRequest request = new GetIndexRequest();
        request.indices(getIndexAlias());
        return indicesClient.exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public SearchResponse getById(String id) throws IOException {
        SearchRequest searchRequest = Requests.searchRequest(getIndexAlias());
        QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds(id);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        searchRequest.source(sourceBuilder);
        return getClient().search(searchRequest, RequestOptions.DEFAULT);
    }

    @Override
    public T pojoById(String id) throws IOException {
        SearchResponse response = getById(id);
        if (response == null) {
            return null;
        }
        List<T> list = ElasticsearchUtil.toPojoList(response, getEntityClass());
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<T> pojoListByIds(Collection<String> ids) throws IOException {

        if (CollectionUtil.isEmpty(ids)) {
            return null;
        }

        MultiGetRequest request = new MultiGetRequest();
        for (String id : ids) {
            request.add(new MultiGetRequest.Item(getIndexAlias(), getIndexType(), id));
        }

        MultiGetResponse multiGetResponse = getClient().mget(request, RequestOptions.DEFAULT);
        if (null == multiGetResponse || multiGetResponse.getResponses() == null || multiGetResponse.getResponses().length <= 0) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();
        for (MultiGetItemResponse itemResponse : multiGetResponse.getResponses()) {
            if (itemResponse.isFailed()) {
                log.error("通过id获取文档失败", itemResponse.getFailure().getFailure());
            } else {
                T entity = ElasticsearchUtil.toPojo(itemResponse.getResponse(), getEntityClass());
                if (entity != null) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    @Override
    public String insert(T entity) throws IOException {
        Map<String, Object> map = new HashMap<>();
        BeanUtil.beanToMap(entity, map, CopyOptions.create().ignoreError());
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            builder.field(key, value);
        }
        builder.endObject();

        IndexRequest request = Requests.indexRequest(getIndexAlias()).type(getIndexType()).source(builder);
        if (entity.getId() != null) {
            request.id(entity.getId().toString());
        }

        IndexResponse response = getClient().index(request, RequestOptions.DEFAULT);
        if (response == null) {
            return null;
        }
        return response.getId();
    }

    @Override
    public boolean batchInsert(Collection<T> list) throws IOException {

        if (CollectionUtil.isEmpty(list)) {
            return true;
        }

        BulkRequest bulkRequest = new BulkRequest();
        for (T entity : list) {
            Map<String, Object> map = ElasticsearchUtil.toMap(entity);
            IndexRequest request = Requests.indexRequest(getIndexAlias()).type(getIndexType()).source(map);
            if (entity.getId() != null) {
                request.id(entity.getId().toString());
            }
            bulkRequest.add(request);
        }

        BulkResponse response = getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
        return !(response == null || response.hasFailures());
    }

    @Override
    public boolean deleteById(String id) throws IOException {
        return deleteByIds(Collections.singleton(id));
    }

    @Override
    public boolean deleteByIds(Collection<String> ids) throws IOException {

        if (CollectionUtil.isEmpty(ids)) {
            return true;
        }

        BulkRequest bulkRequest = new BulkRequest();
        ids.forEach(id -> {
            DeleteRequest deleteRequest = Requests.deleteRequest(getIndexAlias()).type(getIndexType()).id(id);
            bulkRequest.add(deleteRequest);
        });

        BulkResponse response = getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
        return response != null && !response.hasFailures();
    }

    private BulkProcessor newAsyncBulkProcessor() {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                if (response.hasFailures()) {
                    log.error("Bulk [{}] executed with failures,response = {}", executionId, response.buildFailureMessage());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            }
        };
        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer = (request, bulkListener) -> restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
        bulkProcessor = BulkProcessor.builder(bulkConsumer, listener)
            // 1000条数据请求执行一次bulk
            .setBulkActions(1000)
            // 5mb的数据刷新一次bulk
            .setBulkSize(new ByteSizeValue(5L, ByteSizeUnit.MB))
            // 并发请求数量, 0不并发, 1并发允许执行
            .setConcurrentRequests(2)
            // 固定1s必须刷新一次
            .setFlushInterval(TimeValue.timeValueMillis(1000L))
            // 重试3次，间隔100ms
            .setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueMillis(200L), 3)).build();
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

    @Override
    public void close() {
        IoUtil.close(restHighLevelClient);
    }

}

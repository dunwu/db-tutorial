package io.github.dunwu.javadb.elasticsearch;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.elasticsearch.entity.BaseEsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.Page;
import io.github.dunwu.javadb.elasticsearch.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class ElasticsearchTemplate implements Closeable {

    private final RestHighLevelClient client;

    public ElasticsearchTemplate(RestHighLevelClient client) {
        this.client = client;
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    public BulkProcessor newAsyncBulkProcessor() {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                if (response.hasFailures()) {
                    log.error("【ES】Bulk [{}] executed with failures,response = {}", executionId,
                        response.buildFailureMessage());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            }
        };

        int bulkTimeout = 30;
        int bulkActions = 1000;
        int bulkSize = 5;
        int concurrentRequests = 2;
        int flushInterval = 1000;
        int retryInterval = 100;
        int retryLimit = 3;
        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
            (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
        BackoffPolicy backoffPolicy =
            BackoffPolicy.constantBackoff(TimeValue.timeValueMillis(retryInterval), retryLimit);
        BulkProcessor bulkProcessor = BulkProcessor.builder(bulkConsumer, listener)
                                                   // 1000条数据请求执行一次bulk
                                                   .setBulkActions(bulkActions)
                                                   // 5mb的数据刷新一次bulk
                                                   .setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.MB))
                                                   // 并发请求数量, 0不并发, 1并发允许执行
                                                   .setConcurrentRequests(concurrentRequests)
                                                   // 刷新间隔时间
                                                   .setFlushInterval(TimeValue.timeValueMillis(flushInterval))
                                                   // 重试次数、间隔时间
                                                   .setBackoffPolicy(backoffPolicy).build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                bulkProcessor.flush();
                bulkProcessor.awaitClose(bulkTimeout, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("【ES】Failed to close bulkProcessor", e);
            }
            log.info("【ES】bulkProcessor closed!");
        }));
        return bulkProcessor;
    }

    public <T extends BaseEsEntity> T save(String index, String type, T entity) throws IOException {

        if (entity == null) {
            log.warn("【ES】save 实体为空！");
            return null;
        }

        Map<String, Object> map = toMap(entity);
        if (MapUtil.isEmpty(map)) {
            log.warn("【ES】save 实体数据为空！");
            return null;
        }

        IndexRequest request = new IndexRequest(index, type).source(map);
        if (entity.getDocId() != null) {
            request.id(entity.getDocId());
        }
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        if (response == null) {
            log.warn("【ES】save 响应结果为空！");
            return null;
        }

        if (response.getResult() == DocWriteResponse.Result.CREATED
            || response.getResult() == DocWriteResponse.Result.UPDATED) {
            return entity;
        } else {
            log.warn("【ES】save 响应结果无效！result: {}", response.getResult());
            return null;
        }
    }

    public <T extends BaseEsEntity> boolean batchSave(String index, String type, Collection<T> list)
        throws IOException {

        if (CollectionUtil.isEmpty(list)) {
            return true;
        }

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        for (T entity : list) {
            Map<String, Object> map = toMap(entity);
            if (MapUtil.isEmpty(map)) {
                continue;
            }
            IndexRequest request = new IndexRequest(index, type).source(map);
            if (entity.getDocId() != null) {
                request.id(entity.getDocId());
            }
            bulkRequest.add(request);
        }

        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return response != null && !response.hasFailures();
    }

    public <T extends BaseEsEntity> void asyncBatchSave(String index, String type, Collection<T> list,
        ActionListener<BulkResponse> listener) {

        if (CollectionUtil.isEmpty(list)) {
            return;
        }

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        for (T entity : list) {
            Map<String, Object> map = toMap(entity);
            if (MapUtil.isEmpty(map)) {
                continue;
            }
            IndexRequest request = new IndexRequest(index, type).source(map);
            if (entity.getDocId() != null) {
                request.id(entity.getDocId());
            }
            bulkRequest.add(request);
        }

        client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener);
    }

    public <T extends BaseEsEntity> T updateById(String index, String type, T entity) throws IOException {

        if (entity == null) {
            log.warn("【ES】updateById 实体为空！");
            return null;
        }

        if (entity.getDocId() == null) {
            log.warn("【ES】updateById docId 为空！");
            return null;
        }

        Map<String, Object> map = toMap(entity);
        if (MapUtil.isEmpty(map)) {
            log.warn("【ES】updateById 实体数据为空！");
            return null;
        }

        UpdateRequest request = new UpdateRequest(index, type, entity.getDocId()).doc(map);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        if (response == null) {
            log.warn("【ES】updateById 响应结果为空！");
            return null;
        }

        if (response.getResult() == DocWriteResponse.Result.UPDATED) {
            return entity;
        } else {
            log.warn("【ES】updateById 响应结果无效！result: {}", response.getResult());
            return null;
        }
    }

    public <T extends BaseEsEntity> boolean batchUpdateById(String index, String type, Collection<T> list)
        throws IOException {

        if (CollectionUtil.isEmpty(list)) {
            return true;
        }

        BulkRequest bulkRequest = toUpdateBulkRequest(index, type, list);
        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return response != null && !response.hasFailures();
    }

    public <T extends BaseEsEntity> void asyncBatchUpdateById(String index, String type, Collection<T> list,
        ActionListener<BulkResponse> listener) {

        if (CollectionUtil.isEmpty(list)) {
            return;
        }

        BulkRequest bulkRequest = toUpdateBulkRequest(index, type, list);
        client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener);
    }

    private <T extends BaseEsEntity> BulkRequest toUpdateBulkRequest(String index, String type, Collection<T> list) {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        for (T entity : list) {
            if (entity == null || entity.getDocId() == null) {
                continue;
            }
            Map<String, Object> map = toMap(entity);
            if (MapUtil.isEmpty(map)) {
                continue;
            }
            UpdateRequest request = new UpdateRequest(index, type, entity.getDocId()).doc(map);
            bulkRequest.add(request);
        }
        return bulkRequest;
    }

    public boolean deleteById(String index, String type, String id) throws IOException {
        return batchDeleteById(index, type, Collections.singleton(id));
    }

    public boolean batchDeleteById(String index, String type, Collection<String> ids) throws IOException {

        if (CollectionUtil.isEmpty(ids)) {
            return true;
        }

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        ids.stream().filter(Objects::nonNull).forEach(id -> {
            DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
            bulkRequest.add(deleteRequest);
        });

        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (response == null) {
            log.warn("【ES】batchDeleteById 响应结果为空！");
            return false;
        }

        return !response.hasFailures();
    }

    public void asyncBatchDeleteById(String index, String type, Collection<String> ids,
        ActionListener<BulkResponse> listener) {

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

    public GetResponse getById(String index, String type, String id) throws IOException {
        return getById(index, type, id, null);
    }

    public GetResponse getById(String index, String type, String id, Long version) throws IOException {
        GetRequest getRequest = new GetRequest(index, type, id);
        if (version != null) {
            getRequest.version(version);
        }
        return client.get(getRequest, RequestOptions.DEFAULT);
    }

    public <T> T pojoById(String index, String type, String id, Class<T> clazz) throws IOException {
        return pojoById(index, type, id, null, clazz);
    }

    public <T> T pojoById(String index, String type, String id, Long version, Class<T> clazz) throws IOException {
        GetResponse response = getById(index, type, id, version);
        if (response == null) {
            return null;
        }
        return toPojo(response, clazz);
    }

    public <T> List<T> pojoListByIds(String index, String type, Collection<String> ids, Class<T> clazz)
        throws IOException {

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
                T entity = toPojo(itemResponse.getResponse(), clazz);
                if (entity != null) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    public <T> Page<T> pojoPage(String index, String type, SearchSourceBuilder builder, Class<T> clazz)
        throws IOException {
        SearchResponse response = query(index, type, builder);
        if (response == null || response.status() != RestStatus.OK) {
            return null;
        }

        List<T> content = toPojoList(response, clazz);
        SearchHits searchHits = response.getHits();
        int offset = builder.from();
        int size = builder.size();
        int page = offset / size + (offset % size == 0 ? 0 : 1) + 1;
        return new Page<>(page, size, searchHits.getTotalHits(), content);
    }

    public long count(String index, String type, SearchSourceBuilder builder) throws IOException {
        SearchResponse response = query(index, type, builder);
        if (response == null || response.status() != RestStatus.OK) {
            return -1L;
        }
        SearchHits searchHits = response.getHits();
        return searchHits.getTotalHits();
    }

    public SearchResponse query(String index, String type, SearchSourceBuilder builder) throws IOException {
        SearchRequest request = new SearchRequest(index).types(type);
        request.source(builder);
        return client.search(request, RequestOptions.DEFAULT);
    }

    public SearchResponse query(SearchRequest request) throws IOException {
        return client.search(request, RequestOptions.DEFAULT);
    }

    public <T> T toPojo(GetResponse response, Class<T> clazz) {
        if (null == response) {
            return null;
        } else if (StrUtil.isBlank(response.getSourceAsString())) {
            return null;
        } else {
            return JsonUtil.toBean(response.getSourceAsString(), clazz);
        }
    }

    public <T> List<T> toPojoList(SearchResponse response, Class<T> clazz) {

        if (response == null || response.status() != RestStatus.OK) {
            return new ArrayList<>();
        }

        if (ArrayUtil.isEmpty(response.getHits().getHits())) {
            return new ArrayList<>();
        }

        return Stream.of(response.getHits().getHits())
                     .map(hit -> JsonUtil.toBean(hit.getSourceAsString(), clazz))
                     .collect(Collectors.toList());
    }

    public <T> Map<String, Object> toMap(T entity) {
        return JsonUtil.toMap(JsonUtil.toString(entity));
    }

    @Override
    public synchronized void close() {
        if (null == client) {
            return;
        }
        IoUtil.close(client);
    }

}

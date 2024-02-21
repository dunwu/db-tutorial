package io.github.dunwu.javadb.elasticsearch.mapper;

import io.github.dunwu.javadb.elasticsearch.ElasticsearchTemplate;
import io.github.dunwu.javadb.elasticsearch.entity.BaseEsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.Page;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * ES Mapper 基础类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
@Slf4j
public abstract class BaseEsMapper<T extends BaseEsEntity> implements EsMapper<T> {

    private BulkProcessor bulkProcessor;

    protected final ElasticsearchTemplate elasticsearchTemplate;

    public BaseEsMapper(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public RestHighLevelClient getClient() {
        if (elasticsearchTemplate == null) {
            return null;
        }
        return elasticsearchTemplate.getClient();
    }

    @Override
    public synchronized BulkProcessor getBulkProcessor() {
        if (bulkProcessor == null) {
            bulkProcessor = elasticsearchTemplate.newAsyncBulkProcessor();
        }
        return bulkProcessor;
    }

    @Override
    public boolean isIndexExists() throws IOException {
        IndicesClient indicesClient = getClient().indices();
        GetIndexRequest request = new GetIndexRequest();
        request.indices(getIndex());
        return indicesClient.exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public GetResponse getById(String id) throws IOException {
        return getById(id, null);
    }

    @Override
    public GetResponse getById(String id, Long version) throws IOException {
        return elasticsearchTemplate.getById(getIndex(), getType(), id, version);
    }

    @Override
    public T pojoById(String id) throws IOException {
        return pojoById(id, null);
    }

    @Override
    public T pojoById(String id, Long version) throws IOException {
        return elasticsearchTemplate.pojoById(getIndex(), getType(), id, version, getEntityClass());
    }

    @Override
    public List<T> pojoListByIds(Collection<String> ids) throws IOException {
        return elasticsearchTemplate.pojoListByIds(getIndex(), getType(), ids, getEntityClass());
    }

    @Override
    public Page<T> pojoPage(SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.pojoPage(getIndex(), getType(), builder, getEntityClass());
    }

    @Override
    public long count(SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.count(getIndex(), getType(), builder);
    }

    @Override
    public SearchResponse query(SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.query(getIndex(), getType(), builder);
    }

    @Override
    public T save(T entity) throws IOException {
        return elasticsearchTemplate.save(getIndex(), getType(), entity);
    }

    @Override
    public boolean batchSave(Collection<T> list) throws IOException {
        return elasticsearchTemplate.batchSave(getIndex(), getType(), list);
    }

    @Override
    public void asyncBatchSave(Collection<T> list) throws IOException {
        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse response) {
                if (response != null && !response.hasFailures()) {
                    log.info("【ES】异步批量插入成功！");
                } else {
                    log.warn("【ES】异步批量插入失败！");
                }
            }

            @Override
            public void onFailure(Exception e) {
                log.error("【ES】异步批量插入异常！", e);
            }
        };
        asyncBatchSave(list, listener);
    }

    @Override
    public void asyncBatchSave(Collection<T> list, ActionListener<BulkResponse> listener) {
        elasticsearchTemplate.asyncBatchSave(getIndex(), getType(), list, listener);
    }

    @Override
    public T updateById(T entity) throws IOException {
        return elasticsearchTemplate.updateById(getIndex(), getType(), entity);
    }

    @Override
    public boolean batchUpdateById(Collection<T> list) throws IOException {
        return elasticsearchTemplate.batchUpdateById(getIndex(), getType(), list);
    }

    @Override
    public void asyncBatchUpdateById(Collection<T> list, ActionListener<BulkResponse> listener) {
        elasticsearchTemplate.asyncBatchUpdateById(getIndex(), getType(), list, listener);
    }

    @Override
    public boolean deleteById(String id) throws IOException {
        return elasticsearchTemplate.deleteById(getIndex(), getType(), id);
    }

    @Override
    public boolean batchDeleteById(Collection<String> ids) throws IOException {
        return elasticsearchTemplate.batchDeleteById(getIndex(), getType(), ids);
    }

    @Override
    public void asyncBatchDeleteById(Collection<String> ids) throws IOException {
        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse response) {
                if (response != null && !response.hasFailures()) {
                    log.info("【ES】异步批量删除成功！");
                } else {
                    log.warn("【ES】异步批量删除失败！ids: {}", ids);
                }
            }

            @Override
            public void onFailure(Exception e) {
                log.error("【ES】异步批量删除异常！ids: {}", ids, e);
            }
        };
        asyncBatchDeleteById(ids, listener);
    }

    @Override
    public void asyncBatchDeleteById(Collection<String> ids, ActionListener<BulkResponse> listener) throws IOException {
        elasticsearchTemplate.asyncBatchDeleteById(getIndex(), getType(), ids, listener);
    }

}

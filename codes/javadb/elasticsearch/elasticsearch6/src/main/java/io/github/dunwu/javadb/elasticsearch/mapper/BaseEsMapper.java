package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.lang.Assert;
import io.github.dunwu.javadb.elasticsearch.entity.EsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.Page;
import io.github.dunwu.javadb.elasticsearch.util.ElasticsearchUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkResponse;
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
public abstract class BaseEsMapper<T extends EsEntity> implements EsMapper<T> {

    private BulkProcessor bulkProcessor;

    protected final RestHighLevelClient restHighLevelClient;

    public BaseEsMapper(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public RestHighLevelClient getClient() throws IOException {
        Assert.notNull(restHighLevelClient, () -> new IOException("【ES】not connected."));
        return restHighLevelClient;
    }

    @Override
    public synchronized BulkProcessor getBulkProcessor() throws IOException {
        if (bulkProcessor == null) {
            bulkProcessor = ElasticsearchUtil.newAsyncBulkProcessor(getClient());
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
    public SearchResponse getById(String id) throws IOException {
        return ElasticsearchUtil.getById(getClient(), getIndex(), getType(), id);
    }

    @Override
    public T pojoById(String id) throws IOException {
        return ElasticsearchUtil.pojoById(getClient(), getIndex(), getType(), id, getEntityClass());
    }

    @Override
    public List<T> pojoListByIds(Collection<String> ids) throws IOException {
        return ElasticsearchUtil.pojoListByIds(getClient(), getIndex(), getType(), ids, getEntityClass());
    }

    @Override
    public Page<T> pojoPage(SearchSourceBuilder builder) throws IOException {
        return ElasticsearchUtil.pojoPage(getClient(), getIndex(), getType(), builder, getEntityClass());
    }

    @Override
    public String insert(T entity) throws IOException {
        return ElasticsearchUtil.insert(getClient(), getIndex(), getType(), entity);
    }

    @Override
    public boolean batchInsert(Collection<T> list) throws IOException {
        return ElasticsearchUtil.batchInsert(getClient(), getIndex(), getType(), list);
    }

    @Override
    public void asyncBatchInsert(Collection<T> list) throws IOException {
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
        asyncBatchInsert(list, listener);
    }

    @Override
    public void asyncBatchInsert(Collection<T> list, ActionListener<BulkResponse> listener) throws IOException {
        ElasticsearchUtil.asyncBatchInsert(getClient(), getIndex(), getType(), list, listener);
    }

    @Override
    public boolean updateById(T entity) throws IOException {
        return ElasticsearchUtil.updateById(getClient(), getIndex(), getType(), entity);
    }

    @Override
    public boolean batchUpdateById(Collection<T> list) throws IOException {
        return ElasticsearchUtil.batchUpdateById(getClient(), getIndex(), getType(), list);
    }

    @Override
    public void asyncBatchUpdateById(Collection<T> list) throws IOException {
        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse response) {
                if (response != null && !response.hasFailures()) {
                    log.info("【ES】异步批量更新成功！");
                } else {
                    log.warn("【ES】异步批量更新失败！");
                }
            }

            @Override
            public void onFailure(Exception e) {
                log.error("【ES】异步批量更新异常！", e);
            }
        };
        asyncBatchUpdateById(list, listener);
    }

    @Override
    public void asyncBatchUpdateById(Collection<T> list, ActionListener<BulkResponse> listener) throws IOException {
        ElasticsearchUtil.asyncBatchUpdateById(getClient(), getIndex(), getType(), list, listener);
    }

    @Override
    public boolean deleteById(String id) throws IOException {
        return ElasticsearchUtil.deleteById(getClient(), getIndex(), getType(), id);
    }

    @Override
    public boolean batchDeleteById(Collection<String> ids) throws IOException {
        return ElasticsearchUtil.batchDeleteById(getClient(), getIndex(), getType(), ids);
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
        ElasticsearchUtil.asyncBatchDeleteById(getClient(), getIndex(), getType(), ids, listener);
    }

}

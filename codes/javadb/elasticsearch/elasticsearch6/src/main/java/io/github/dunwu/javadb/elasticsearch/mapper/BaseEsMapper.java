package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.elasticsearch.ElasticsearchTemplate;
import io.github.dunwu.javadb.elasticsearch.constant.ResultCode;
import io.github.dunwu.javadb.elasticsearch.entity.BaseEsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.common.PageData;
import io.github.dunwu.javadb.elasticsearch.entity.common.ScrollData;
import io.github.dunwu.javadb.elasticsearch.exception.DefaultException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES Mapper 基础类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
@Slf4j
public abstract class BaseEsMapper<T extends BaseEsEntity> implements EsMapper<T> {

    protected BulkProcessor bulkProcessor;

    protected final ElasticsearchTemplate elasticsearchTemplate;

    public BaseEsMapper(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    public int getShard() {
        return 5;
    }

    public int getReplica() {
        return 1;
    }

    /**
     * 如果开启，添加 ES 数据时，如果索引不存在，会自动创建索引
     */
    public boolean enableAutoCreateIndex() {
        return true;
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

    @SuppressWarnings("unchecked")
    public Map<String, String> getPropertiesMap() {

        Class<T> clazz = getEntityClass();
        Method method;
        try {
            method = clazz.getMethod("getPropertiesMap");
        } catch (NoSuchMethodException e) {
            String msg = StrUtil.format("【ES】检查并创建 {} 索引失败！day 不能为空！", getAlias());
            throw new DefaultException(e, ResultCode.ERROR, msg);
        }

        Object result = ReflectUtil.invokeStatic(method);
        if (result == null) {
            return new HashMap<>(0);
        }
        return (Map<String, String>) result;
    }

    // ====================================================================
    // 索引管理操作
    // ====================================================================

    @Override
    public boolean isIndexExists() throws IOException {
        return elasticsearchTemplate.isIndexExists(getIndex());
    }

    @Override
    public String createIndexIfNotExists() throws IOException {
        String index = getIndex();
        boolean exists = elasticsearchTemplate.isIndexExists(index);
        if (exists) {
            return index;
        }
        elasticsearchTemplate.createIndex(index, getType(), getAlias(), getShard(), getReplica());
        Map<String, String> propertiesMap = getPropertiesMap();
        if (MapUtil.isNotEmpty(propertiesMap)) {
            elasticsearchTemplate.setMapping(index, getType(), propertiesMap);
        }
        return index;
    }

    @Override
    public void deleteIndex() throws IOException {
        elasticsearchTemplate.deleteIndex(getIndex());
    }

    @Override
    public void updateAlias() throws IOException {
        elasticsearchTemplate.updateAlias(getIndex(), getAlias());
    }

    // ====================================================================
    // CRUD 操作
    // ====================================================================

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
    public long count(SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.count(getIndex(), getType(), builder);
    }

    @Override
    public SearchResponse query(SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.query(getIndex(), getType(), builder);
    }
    @Override
    public PageData<T> pojoPage(SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.pojoPage(getIndex(), getType(), builder, getEntityClass());
    }

    @Override
    public ScrollData<T> pojoPageByLastId(String lastId, int size, QueryBuilder queryBuilder) throws IOException {
        return elasticsearchTemplate.pojoPageByLastId(getIndex(), getType(), lastId, size,
            queryBuilder, getEntityClass());
    }

    @Override
    public ScrollData<T> pojoScrollBegin(SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.pojoScrollBegin(getIndex(), getType(), builder, getEntityClass());
    }

    @Override
    public ScrollData<T> pojoScroll(String scrollId, SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.pojoScroll(scrollId, builder, getEntityClass());
    }

    @Override
    public boolean pojoScrollEnd(String scrollId) throws IOException {
        return elasticsearchTemplate.pojoScrollEnd(scrollId);
    }


    @Override
    public T save(T entity) throws IOException {
        String index = checkIndex();
        checkData(entity);
        return elasticsearchTemplate.save(index, getType(), entity);
    }

    @Override
    public boolean saveBatch(Collection<T> list) throws IOException {
        String index = checkIndex();
        checkData(list);
        return elasticsearchTemplate.saveBatch(index, getType(), list);
    }

    @Override
    public void asyncSaveBatch(Collection<T> list) throws IOException {
        String index = checkIndex();
        checkData(list);
        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse response) {
                if (response != null && !response.hasFailures()) {
                    String msg = StrUtil.format("【ES】异步批量保存 {} 成功！", index);
                    log.info(msg);
                } else {
                    String msg = StrUtil.format("【ES】异步批量保存 {} 失败！", index);
                    log.warn(msg);
                }
            }

            @Override
            public void onFailure(Exception e) {
                String msg = StrUtil.format("【ES】异步批量保存 {} 异常！", index);
                log.error(msg, e);
            }
        };
        asyncSaveBatch(list, listener);
    }

    @Override
    public void asyncSaveBatch(Collection<T> list, ActionListener<BulkResponse> listener) throws IOException {
        String index = checkIndex();
        checkData(list);
        elasticsearchTemplate.asyncSaveBatch(index, getType(), list, listener);
    }

    @Override
    public T updateById(T entity) throws IOException {
        checkData(entity);
        return elasticsearchTemplate.updateById(getIndex(), getType(), entity);
    }

    @Override
    public boolean updateBatchIds(Collection<T> list) throws IOException {
        checkData(list);
        return elasticsearchTemplate.updateBatchIds(getIndex(), getType(), list);
    }

    @Override
    public void asyncUpdateBatchIds(Collection<T> list, ActionListener<BulkResponse> listener) {
        checkData(list);
        elasticsearchTemplate.asyncUpdateBatchIds(getIndex(), getType(), list, listener);
    }

    @Override
    public boolean deleteById(String id) throws IOException {
        return elasticsearchTemplate.deleteById(getIndex(), getType(), id);
    }

    @Override
    public boolean deleteBatchIds(Collection<String> ids) throws IOException {
        return elasticsearchTemplate.deleteBatchIds(getIndex(), getType(), ids);
    }

    @Override
    public void asyncDeleteBatchIds(Collection<String> ids) throws IOException {
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
        asyncDeleteBatchIds(ids, listener);
    }

    @Override
    public void asyncDeleteBatchIds(Collection<String> ids, ActionListener<BulkResponse> listener) throws IOException {
        elasticsearchTemplate.asyncDeleteBatchIds(getIndex(), getType(), ids, listener);
    }

    protected String checkIndex() throws IOException {
        if (!enableAutoCreateIndex()) {
            return getIndex();
        }
        String index = createIndexIfNotExists();
        if (StrUtil.isBlank(index)) {
            String msg = StrUtil.format("【ES】索引找不到且创建失败！", index);
            throw new DefaultException(ResultCode.ERROR, msg);
        }
        return index;
    }

    protected void checkData(Collection<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            String msg = StrUtil.format("【ES】写入 {} 失败！list 不能为空！", getIndex());
            throw new DefaultException(ResultCode.PARAM_ERROR, msg);
        }
    }

    protected void checkData(T entity) {
        if (entity == null) {
            String msg = StrUtil.format("【ES】写入 {} 失败！entity 不能为空！", getIndex());
            throw new DefaultException(ResultCode.PARAM_ERROR, msg);
        }
    }

}

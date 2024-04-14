package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            log.error("【ES】{} 中不存在 getPropertiesMap 方法！", clazz.getCanonicalName());
            return new HashMap<>(0);
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
    public boolean isIndexExists() {
        String index = getIndex();
        try {
            return elasticsearchTemplate.isIndexExists(index);
        } catch (Exception e) {
            log.error("【ES】判断索引是否存在异常！index: {}", index, e);
            return false;
        }
    }

    @Override
    public String createIndexIfNotExists() {
        String index = getIndex();
        String type = getType();
        String alias = getAlias();
        int shard = getShard();
        int replica = getReplica();
        return createIndex(index, type, alias, shard, replica);
    }

    protected String createIndex(String index, String type, String alias, int shard, int replica) {
        try {
            if (elasticsearchTemplate.isIndexExists(index)) {
                return index;
            }
            elasticsearchTemplate.createIndex(index, type, alias, shard, replica);
            log.info("【ES】创建索引成功！index: {}, type: {}, alias: {}, shard: {}, replica: {}",
                index, type, alias, shard, replica);
            Map<String, String> propertiesMap = getPropertiesMap();
            if (MapUtil.isNotEmpty(propertiesMap)) {
                elasticsearchTemplate.setMapping(index, type, propertiesMap);
                log.error("【ES】设置索引 mapping 成功！index: {}, type: {}, propertiesMap: {}",
                    index, type, JSONUtil.toJsonStr(propertiesMap));
            }
            return index;
        } catch (Exception e) {
            log.error("【ES】创建索引异常！index: {}, type: {}, alias: {}, shard: {}, replica: {}",
                index, type, alias, shard, replica, e);
            return null;
        }
    }

    @Override
    public void deleteIndex() {
        String index = getIndex();
        try {
            log.info("【ES】删除索引成功！index: {}", index);
            elasticsearchTemplate.deleteIndex(index);
        } catch (Exception e) {
            log.error("【ES】删除索引异常！index: {}", index, e);
        }
    }

    @Override
    public void updateAlias() {
        String index = getIndex();
        String alias = getAlias();
        try {
            log.info("【ES】更新别名成功！alias: {} -> index: {}", alias, index);
            elasticsearchTemplate.updateAlias(index, alias);
        } catch (Exception e) {
            log.error("【ES】更新别名异常！alias: {} -> index: {}", alias, index, e);
        }
    }

    @Override
    public Set<String> getIndexSet() {
        String alias = getAlias();
        try {
            return elasticsearchTemplate.getIndexSet(alias);
        } catch (Exception e) {
            log.error("【ES】获取别名的所有索引异常！alias: {}", alias, e);
            return new HashSet<>(0);
        }
    }

    // ====================================================================
    // CRUD 操作
    // ====================================================================

    @Override
    public GetResponse getById(String id) {
        return getById(id, null);
    }

    @Override
    public GetResponse getById(String id, Long version) {
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.getById(index, type, id, version);
        } catch (Exception e) {
            log.error("【ES】根据ID查询异常！index: {}, type: {}, id: {}, version: {}", index, type, id, version, e);
            return null;
        }
    }

    @Override
    public T pojoById(String id) {
        return pojoById(id, null);
    }

    @Override
    public T pojoById(String id, Long version) {
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.pojoById(index, type, id, version, getEntityClass());
        } catch (Exception e) {
            log.error("【ES】根据ID查询POJO异常！index: {}, type: {}, id: {}, version: {}", index, type, id, version, e);
            return null;
        }
    }

    @Override
    public List<T> pojoListByIds(Collection<String> ids) {
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.pojoListByIds(index, type, ids, getEntityClass());
        } catch (Exception e) {
            log.error("【ES】根据ID查询POJO列表异常！index: {}, type: {}, ids: {}", index, type, ids, e);
            return new ArrayList<>(0);
        }
    }

    @Override
    public long count(SearchSourceBuilder builder) {
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.count(index, type, builder);
        } catch (Exception e) {
            log.error("【ES】获取匹配记录数异常！index: {}, type: {}", index, type, e);
            return 0L;
        }
    }

    @Override
    public SearchResponse query(SearchSourceBuilder builder) {
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.query(index, type, builder);
        } catch (Exception e) {
            log.error("【ES】条件查询异常！index: {}, type: {}", index, type, e);
            return null;
        }
    }

    @Override
    public PageData<T> pojoPage(SearchSourceBuilder builder) {
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.pojoPage(index, type, builder, getEntityClass());
        } catch (Exception e) {
            log.error("【ES】from + size 分页条件查询异常！index: {}, type: {}", index, type, e);
            return null;
        }
    }

    @Override
    public ScrollData<T> pojoPageByLastId(String scrollId, int size, QueryBuilder queryBuilder) {
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.pojoPageByScrollId(index, type, scrollId, size, queryBuilder,
                getEntityClass());
        } catch (Exception e) {
            log.error("【ES】search after 分页条件查询异常！index: {}, type: {}", index, type, e);
            return null;
        }
    }

    @Override
    public ScrollData<T> pojoScrollBegin(SearchSourceBuilder builder) {
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.pojoScrollBegin(index, type, builder, getEntityClass());
        } catch (Exception e) {
            log.error("【ES】开启滚动分页条件查询异常！index: {}, type: {}", index, type, e);
            return null;
        }
    }

    @Override
    public ScrollData<T> pojoScroll(String scrollId, SearchSourceBuilder builder) {
        try {
            return elasticsearchTemplate.pojoScroll(scrollId, builder, getEntityClass());
        } catch (Exception e) {
            log.error("【ES】滚动分页条件查询异常！scrollId: {}", scrollId, e);
            return null;
        }
    }

    @Override
    public boolean pojoScrollEnd(String scrollId) {
        try {
            return elasticsearchTemplate.pojoScrollEnd(scrollId);
        } catch (Exception e) {
            log.error("【ES】关闭滚动分页条件查询异常！scrollId: {}", scrollId, e);
            return false;
        }
    }

    @Override
    public T save(T entity) {
        if (entity == null) {
            return null;
        }
        String index = getIndex();
        String type = getType();
        try {
            checkIndex();
            checkData(entity);
            return elasticsearchTemplate.save(index, type, entity);
        } catch (Exception e) {
            log.error("【ES】添加数据异常！index: {}, type: {}, entity: {}", index, type, JSONUtil.toJsonStr(entity), e);
            return null;
        }
    }

    @Override
    public boolean saveBatch(Collection<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }
        String index = getIndex();
        String type = getType();
        try {
            checkIndex();
            checkData(list);
            return elasticsearchTemplate.saveBatch(index, type, list);
        } catch (Exception e) {
            log.error("【ES】批量添加数据异常！index: {}, type: {}, size: {}", index, type, list.size(), e);
            return false;
        }
    }

    @Override
    public void asyncSaveBatch(Collection<T> list) {
        asyncSaveBatch(list, DEFAULT_BULK_LISTENER);
    }

    @Override
    public void asyncSaveBatch(Collection<T> list, ActionListener<BulkResponse> listener) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        String index = getIndex();
        String type = getType();
        try {
            checkIndex();
            checkData(list);
            elasticsearchTemplate.asyncSaveBatch(index, getType(), list, listener);
        } catch (Exception e) {
            log.error("【ES】异步批量添加数据异常！index: {}, type: {}, size: {}", index, type, list.size(), e);
        }
    }

    @Override
    public T updateById(T entity) {
        if (entity == null) {
            return null;
        }
        String index = getIndex();
        String type = getType();
        try {
            checkData(entity);
            return elasticsearchTemplate.updateById(index, type, entity);
        } catch (Exception e) {
            log.error("【ES】更新数据异常！index: {}, type: {}", index, type, e);
            return null;
        }
    }

    @Override
    public boolean updateBatchIds(Collection<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }
        String index = getIndex();
        String type = getType();
        try {
            checkData(list);
            return elasticsearchTemplate.updateBatchIds(index, type, list);
        } catch (Exception e) {
            log.error("【ES】批量更新数据异常！index: {}, type: {}, size: {}", index, type, list.size(), e);
            return false;
        }
    }

    @Override
    public void asyncUpdateBatchIds(Collection<T> list) {
        asyncUpdateBatchIds(list, DEFAULT_BULK_LISTENER);
    }

    @Override
    public void asyncUpdateBatchIds(Collection<T> list, ActionListener<BulkResponse> listener) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        String index = getIndex();
        String type = getType();
        try {
            checkData(list);
            elasticsearchTemplate.asyncUpdateBatchIds(index, type, list, listener);
        } catch (Exception e) {
            log.error("【ES】异步批量更新数据异常！index: {}, type: {}, size: {}", index, type, list.size(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (StrUtil.isBlank(id)) {
            return false;
        }
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.deleteById(index, type, id);
        } catch (Exception e) {
            log.error("【ES】根据ID删除数据异常！index: {}, type: {}, id: {}", index, type, id, e);
            return false;
        }
    }

    @Override
    public boolean deleteBatchIds(Collection<String> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return false;
        }
        String index = getIndex();
        String type = getType();
        try {
            return elasticsearchTemplate.deleteBatchIds(index, type, ids);
        } catch (Exception e) {
            log.error("【ES】根据ID批量删除数据异常！index: {}, type: {}, ids: {}", index, type, ids, e);
            return false;
        }
    }

    @Override
    public void asyncDeleteBatchIds(Collection<String> ids) {
        asyncDeleteBatchIds(ids, DEFAULT_BULK_LISTENER);
    }

    @Override
    public void asyncDeleteBatchIds(Collection<String> ids, ActionListener<BulkResponse> listener) {
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        String index = getIndex();
        String type = getType();
        try {
            elasticsearchTemplate.asyncDeleteBatchIds(index, type, ids, listener);
        } catch (Exception e) {
            log.error("【ES】异步根据ID批量删除数据异常！index: {}, type: {}, ids: {}", index, type, ids, e);
        }
    }

    protected String checkIndex() {
        if (!enableAutoCreateIndex()) {
            return getIndex();
        }
        String index = createIndexIfNotExists();
        if (StrUtil.isBlank(index)) {
            String msg = StrUtil.format("【ES】索引 {} 找不到且创建失败！", index);
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

    protected final ActionListener<BulkResponse> DEFAULT_BULK_LISTENER = new ActionListener<BulkResponse>() {
        @Override
        public void onResponse(BulkResponse response) {
            if (response != null && !response.hasFailures()) {
                log.info("【ES】异步批量写数据成功！index: {}, type: {}", getIndex(), getType());
            } else {
                log.warn("【ES】异步批量写数据失败！index: {}, type: {}", getIndex(), getType());
            }
        }

        @Override
        public void onFailure(Exception e) {
            log.error("【ES】异步批量写数据异常！index: {}, type: {}", getIndex(), getType());
        }
    };

}

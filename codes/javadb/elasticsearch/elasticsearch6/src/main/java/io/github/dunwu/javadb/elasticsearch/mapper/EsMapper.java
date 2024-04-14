package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.collection.CollectionUtil;
import io.github.dunwu.javadb.elasticsearch.entity.BaseEsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.common.PageData;
import io.github.dunwu.javadb.elasticsearch.entity.common.ScrollData;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ES Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
public interface EsMapper<T extends BaseEsEntity> {

    /**
     * 获取别名
     */
    String getAlias();

    /**
     * 获取索引名
     */
    String getIndex();

    /**
     * 获取索引类型
     */
    String getType();

    /**
     * 获取分片数
     */
    int getShard();

    /**
     * 获取副本数
     */
    int getReplica();

    /**
     * 获取实体类型
     */
    Class<T> getEntityClass();

    /**
     * 如果开启，添加 ES 数据时，如果索引不存在，会自动创建索引
     */
    default boolean enableAutoCreateIndex() {
        return false;
    }

    RestHighLevelClient getClient();

    BulkProcessor getBulkProcessor();

    boolean isIndexExists();

    String createIndexIfNotExists();

    void deleteIndex();

    void updateAlias();

    Set<String> getIndexSet();

    GetResponse getById(String id);

    GetResponse getById(String id, Long version);

    T pojoById(String id);

    T pojoById(String id, Long version);

    List<T> pojoListByIds(Collection<String> ids);

    default Map<String, T> pojoMapByIds(Collection<String> ids) {
        List<T> list = pojoListByIds(ids);
        if (CollectionUtil.isEmpty(list)) {
            return new HashMap<>(0);
        }

        Map<String, T> map = new HashMap<>(list.size());
        for (T entity : list) {
            map.put(entity.getDocId(), entity);
        }
        return map;
    }

    long count(SearchSourceBuilder builder);

    SearchResponse query(SearchSourceBuilder builder);

    PageData<T> pojoPage(SearchSourceBuilder builder);

    ScrollData<T> pojoPageByLastId(String scrollId, int size, QueryBuilder queryBuilder);

    ScrollData<T> pojoScrollBegin(SearchSourceBuilder builder);

    ScrollData<T> pojoScroll(String scrollId, SearchSourceBuilder builder);

    boolean pojoScrollEnd(String scrollId);

    T save(T entity);

    boolean saveBatch(Collection<T> list);

    void asyncSaveBatch(Collection<T> list);

    void asyncSaveBatch(Collection<T> list, ActionListener<BulkResponse> listener);

    T updateById(T entity);

    boolean updateBatchIds(Collection<T> list);

    void asyncUpdateBatchIds(Collection<T> list);

    void asyncUpdateBatchIds(Collection<T> list, ActionListener<BulkResponse> listener);

    boolean deleteById(String id);

    boolean deleteBatchIds(Collection<String> ids);

    void asyncDeleteBatchIds(Collection<String> ids);

    void asyncDeleteBatchIds(Collection<String> ids, ActionListener<BulkResponse> listener);

}

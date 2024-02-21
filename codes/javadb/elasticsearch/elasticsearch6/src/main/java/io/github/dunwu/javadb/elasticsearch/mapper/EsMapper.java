package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.collection.CollectionUtil;
import io.github.dunwu.javadb.elasticsearch.entity.BaseEsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.Page;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
public interface EsMapper<T extends BaseEsEntity> {

    /**
     * 获取索引名
     */
    String getIndex();

    /**
     * 获取索引类型
     */
    String getType();

    /**
     * 获取实体类型
     */
    Class<T> getEntityClass();

    RestHighLevelClient getClient() throws IOException;

    BulkProcessor getBulkProcessor() throws IOException;

    boolean isIndexExists() throws IOException;

    GetResponse getById(String id) throws IOException;

    GetResponse getById(String id, Long version) throws IOException;

    T pojoById(String id) throws IOException;

    T pojoById(String id, Long version) throws IOException;

    List<T> pojoListByIds(Collection<String> ids) throws IOException;

    default Map<String, T> pojoMapByIds(Collection<String> ids) throws IOException {
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

    Page<T> pojoPage(SearchSourceBuilder builder) throws IOException;

    long count(SearchSourceBuilder builder) throws IOException;

    SearchResponse query(SearchSourceBuilder builder) throws IOException;

    T save(T entity) throws IOException;

    boolean batchSave(Collection<T> list) throws IOException;

    void asyncBatchSave(Collection<T> list) throws IOException;

    void asyncBatchSave(Collection<T> list, ActionListener<BulkResponse> listener) throws IOException;

    T updateById(T entity) throws IOException;

    boolean batchUpdateById(Collection<T> list) throws IOException;

    void asyncBatchUpdateById(Collection<T> list, ActionListener<BulkResponse> listener);

    boolean deleteById(String id) throws IOException;

    boolean batchDeleteById(Collection<String> ids) throws IOException;

    void asyncBatchDeleteById(Collection<String> ids) throws IOException;

    void asyncBatchDeleteById(Collection<String> ids, ActionListener<BulkResponse> listener) throws IOException;

}

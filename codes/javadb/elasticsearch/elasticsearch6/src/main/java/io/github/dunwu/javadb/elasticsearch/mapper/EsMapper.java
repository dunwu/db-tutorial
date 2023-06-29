package io.github.dunwu.javadb.elasticsearch.mapper;

import io.github.dunwu.javadb.elasticsearch.entity.EsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.Page;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * ES Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-27
 */
public interface EsMapper<T extends EsEntity> {

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

    SearchResponse getById(String id) throws IOException;

    T pojoById(String id) throws IOException;

    List<T> pojoListByIds(Collection<String> ids) throws IOException;

    Page<T> pojoPage(SearchSourceBuilder builder) throws IOException;

    String insert(T entity) throws IOException;

    boolean batchInsert(Collection<T> list) throws IOException;

    void asyncBatchInsert(Collection<T> list) throws IOException;

    void asyncBatchInsert(Collection<T> list, ActionListener<BulkResponse> listener) throws IOException;

    boolean updateById(T entity) throws IOException;

    boolean batchUpdateById(Collection<T> list) throws IOException;

    void asyncBatchUpdateById(Collection<T> list) throws IOException;

    void asyncBatchUpdateById(Collection<T> list, ActionListener<BulkResponse> listener) throws IOException;

    boolean deleteById(String id) throws IOException;

    boolean batchDeleteById(Collection<String> ids) throws IOException;

    void asyncBatchDeleteById(Collection<String> ids) throws IOException;

    void asyncBatchDeleteById(Collection<String> ids, ActionListener<BulkResponse> listener) throws IOException;

}

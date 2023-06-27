package io.github.dunwu.javadb.elasticsearch.mapper;

import io.github.dunwu.javadb.elasticsearch.entity.EsEntity;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;

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
     * 获取索引别名
     */
    String getIndexAlias();

    /**
     * 获取索引名
     */
    String getIndexName();

    /**
     * 获取索引类型
     */
    String getIndexType();

    /**
     * 获取实体类型
     */
    Class<T> getEntityClass();

    RestHighLevelClient getClient() throws IOException;

    BulkProcessor getBulkProcessor();

    boolean isIndexExists() throws IOException;

    SearchResponse getById(String id) throws IOException;

    T pojoById(String id) throws IOException;

    List<T> pojoListByIds(Collection<String> ids) throws IOException;

    String insert(T entity) throws IOException;

    boolean batchInsert(Collection<T> list) throws IOException;

    boolean deleteById(String id) throws IOException;

    boolean deleteByIds(Collection<String> ids) throws IOException;
}

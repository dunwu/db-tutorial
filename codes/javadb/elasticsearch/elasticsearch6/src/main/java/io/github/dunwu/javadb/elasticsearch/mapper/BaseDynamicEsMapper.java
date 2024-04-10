package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.elasticsearch.ElasticsearchTemplate;
import io.github.dunwu.javadb.elasticsearch.constant.ResultCode;
import io.github.dunwu.javadb.elasticsearch.entity.BaseEsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.common.PageData;
import io.github.dunwu.javadb.elasticsearch.entity.common.ScrollData;
import io.github.dunwu.javadb.elasticsearch.exception.DefaultException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 动态 ES Mapper 基础类（以时间为维度动态创建、删除 index），用于数据量特别大，需要按照日期分片的索引。
 * <p>
 * 注：使用此 Mapper 的索引、别名必须遵循命名格式：索引名 = 别名_yyyyMMdd
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2024-04-07
 */
@Slf4j
public abstract class BaseDynamicEsMapper<T extends BaseEsEntity> extends BaseEsMapper<T> {

    public BaseDynamicEsMapper(ElasticsearchTemplate elasticsearchTemplate) {
        super(elasticsearchTemplate);
    }

    // ====================================================================
    // 索引管理操作
    // ====================================================================

    public String getIndex(String day) {

        String alias = getAlias();
        if (StrUtil.isBlank(day)) {
            String msg = StrUtil.format("【ES】获取 {} 索引失败！day 不能为空！", alias);
            throw new DefaultException(ResultCode.PARAM_ERROR, msg);
        }

        DateTime date;
        try {
            date = DateUtil.parse(day, DatePattern.NORM_DATE_PATTERN);
        } catch (Exception e) {
            String msg = StrUtil.format("【ES】获取 {} 索引失败！day: {} 不符合日期格式 {}！",
                alias, day, DatePattern.NORM_DATE_PATTERN);
            throw new DefaultException(e, ResultCode.PARAM_ERROR, msg);
        }

        String formatDate = DateUtil.format(date, DatePattern.PURE_DATE_FORMAT);
        return alias + "_" + formatDate;
    }

    public boolean isIndexExistsInDay(String day) throws IOException {
        return elasticsearchTemplate.isIndexExists(getIndex(day));
    }

    public String createIndexInDay(String day) throws IOException, DefaultException {
        String index = getIndex(day);
        boolean indexExists = isIndexExistsInDay(day);
        if (indexExists) {
            return index;
        }
        elasticsearchTemplate.createIndex(index, getType(), getAlias(), getShard(), getReplica());
        Map<String, String> map = getPropertiesMap();
        if (MapUtil.isNotEmpty(map)) {
            elasticsearchTemplate.setMapping(index, getType(), map);
        }
        return index;
    }

    public void deleteIndexInDay(String day) throws IOException {
        elasticsearchTemplate.deleteIndex(getIndex(day));
    }

    public void updateAliasInDay(String day) throws IOException {
        elasticsearchTemplate.updateAlias(getIndex(day), getAlias());
    }

    // ====================================================================
    // CRUD 操作
    // ====================================================================

    public GetResponse getByIdInDay(String day, String id) throws IOException {
        return elasticsearchTemplate.getById(getIndex(day), getType(), id, null);
    }

    public T pojoByIdInDay(String day, String id) throws IOException {
        return elasticsearchTemplate.pojoById(getIndex(day), getType(), id, null, getEntityClass());
    }

    public List<T> pojoListByIdsInDay(String day, Collection<String> ids) throws IOException {
        return elasticsearchTemplate.pojoListByIds(getIndex(day), getType(), ids, getEntityClass());
    }

    public long countInDay(String day, SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.count(getIndex(day), getType(), builder);
    }

    public SearchResponse queryInDay(String day, SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.query(getIndex(day), getType(), builder);
    }

    public PageData<T> pojoPageInDay(String day, SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.pojoPage(getIndex(day), getType(), builder, getEntityClass());
    }

    public ScrollData<T> pojoPageByLastIdInDay(String day, String lastId, int size, QueryBuilder queryBuilder)
        throws IOException {
        return elasticsearchTemplate.pojoPageByLastId(getIndex(day), getType(), lastId, size,
            queryBuilder, getEntityClass());
    }

    public ScrollData<T> pojoScrollBeginInDay(String day, SearchSourceBuilder builder) throws IOException {
        return elasticsearchTemplate.pojoScrollBegin(getIndex(day), getType(), builder, getEntityClass());
    }

    /**
     * 根据日期动态选择索引并更新
     *
     * @param day    日期，格式为：yyyy-MM-dd
     * @param entity 待更新的数据
     * @return /
     */
    public boolean saveInDay(String day, T entity) throws IOException, DefaultException {
        String index = checkIndex(day);
        checkData(entity);
        elasticsearchTemplate.save(index, getType(), entity);
        return true;
    }

    /**
     * 根据日期动态选择索引并批量更新
     *
     * @param day  日期，格式为：yyyy-MM-dd
     * @param list 待更新的数据
     * @return /
     */
    public boolean saveBatchInDay(String day, Collection<T> list) throws IOException, DefaultException {
        String index = checkIndex(day);
        checkData(list);
        elasticsearchTemplate.saveBatch(index, getType(), list);
        return true;
    }

    public void asyncSaveBatchInDay(String day, Collection<T> list) throws IOException {
        String index = checkIndex(day);
        checkData(list);
        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse response) {
                if (response != null && !response.hasFailures()) {
                    String msg = StrUtil.format("【ES】按日期异步批量保存 {} 成功！", index);
                    log.info(msg);
                } else {
                    String msg = StrUtil.format("【ES】按日期异步批量保存 {} 失败！", index);
                    log.warn(msg);
                }
            }

            @Override
            public void onFailure(Exception e) {
                String msg = StrUtil.format("【ES】按日期异步批量保存 {} 异常！", index);
                log.error(msg, e);
            }
        };
        asyncSaveBatchInDay(day, list, listener);
    }

    public void asyncSaveBatchInDay(String day, Collection<T> list, ActionListener<BulkResponse> listener)
        throws IOException {
        String index = checkIndex(day);
        checkData(list);
        elasticsearchTemplate.asyncSaveBatch(getIndex(day), getType(), list, listener);
    }

    public boolean deleteByIdInDay(String day, String id) throws IOException {
        return elasticsearchTemplate.deleteById(getIndex(day), getType(), id);
    }

    public boolean deleteBatchIdsInDay(String day, Collection<String> ids) throws IOException {
        return elasticsearchTemplate.deleteBatchIds(getIndex(day), getType(), ids);
    }

    protected String checkIndex(String day) throws IOException {
        if (!enableAutoCreateIndex()) {
            return getIndex(day);
        }
        String index = createIndexInDay(day);
        if (StrUtil.isBlank(index)) {
            String msg = StrUtil.format("【ES】按日期批量保存 {} 失败！索引找不到且创建失败！", index);
            throw new DefaultException(ResultCode.ERROR, msg);
        }
        return index;
    }

}

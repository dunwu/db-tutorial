package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
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
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @Override
    public boolean enableAutoCreateIndex() {
        return true;
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

    public boolean isIndexExistsInDay(String day) {
        if (StrUtil.isBlank(day)) {
            return false;
        }
        String index = getIndex(day);
        try {
            return elasticsearchTemplate.isIndexExists(getIndex(day));
        } catch (Exception e) {
            log.error("【ES】判断索引是否存在异常！index: {}", index, e);
            return false;
        }
    }

    public String createIndexIfNotExistsInDay(String day) {
        String index = getIndex(day);
        String type = getType();
        String alias = getAlias();
        int shard = getShard();
        int replica = getReplica();
        return createIndex(index, type, alias, shard, replica);
    }

    public void deleteIndexInDay(String day) {
        String index = getIndex(day);
        try {
            log.info("【ES】删除索引成功！index: {}", index);
            elasticsearchTemplate.deleteIndex(index);
        } catch (Exception e) {
            log.error("【ES】删除索引异常！index: {}", index, e);
        }
    }

    public void updateAliasInDay(String day) {
        String index = getIndex(day);
        String alias = getAlias();
        try {
            log.info("【ES】更新别名成功！alias: {} -> index: {}", alias, index);
            elasticsearchTemplate.updateAlias(index, alias);
        } catch (IOException e) {
            log.error("【ES】更新别名异常！alias: {} -> index: {}", alias, index, e);
        }
    }

    // ====================================================================
    // CRUD 操作
    // ====================================================================

    public GetResponse getByIdInDay(String day, String id) {
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.getById(index, type, id, null);
        } catch (IOException e) {
            log.error("【ES】根据ID查询异常！index: {}, type: {}, id: {}", index, type, id, e);
            return null;
        }
    }

    public T pojoByIdInDay(String day, String id) {
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.pojoById(index, type, id, null, getEntityClass());
        } catch (IOException e) {
            log.error("【ES】根据ID查询POJO异常！index: {}, type: {}, id: {}", index, type, id, e);
            return null;
        }
    }

    public List<T> pojoListByIdsInDay(String day, Collection<String> ids) {
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.pojoListByIds(index, type, ids, getEntityClass());
        } catch (IOException e) {
            log.error("【ES】根据ID查询POJO列表异常！index: {}, type: {}, ids: {}", index, type, ids, e);
            return new ArrayList<>(0);
        }
    }

    public long countInDay(String day, SearchSourceBuilder builder) {
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.count(index, type, builder);
        } catch (IOException e) {
            log.error("【ES】获取匹配记录数异常！index: {}, type: {}", index, type, e);
            return 0L;
        }
    }

    public SearchResponse queryInDay(String day, SearchSourceBuilder builder) {
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.query(index, type, builder);
        } catch (IOException e) {
            log.error("【ES】条件查询异常！index: {}, type: {}", index, type, e);
            return null;
        }
    }

    public PageData<T> pojoPageInDay(String day, SearchSourceBuilder builder) {
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.pojoPage(index, type, builder, getEntityClass());
        } catch (IOException e) {
            log.error("【ES】from + size 分页条件查询异常！index: {}, type: {}", index, type, e);
            return null;
        }
    }

    public ScrollData<T> pojoPageByLastIdInDay(String day, String scrollId, int size, QueryBuilder queryBuilder) {
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.pojoPageByScrollId(index, type, scrollId, size, queryBuilder, getEntityClass());
        } catch (IOException e) {
            log.error("【ES】search after 分页条件查询异常！index: {}, type: {}", index, type, e);
            return null;
        }
    }

    public ScrollData<T> pojoScrollBeginInDay(String day, SearchSourceBuilder builder) {
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.pojoScrollBegin(index, type, builder, getEntityClass());
        } catch (IOException e) {
            log.error("【ES】开启滚动分页条件查询异常！index: {}, type: {}", index, type, e);
            return null;
        }
    }

    /**
     * 根据日期动态选择索引并更新
     *
     * @param day    日期，格式为：yyyy-MM-dd
     * @param entity 待更新的数据
     * @return /
     */
    public T saveInDay(String day, T entity) {
        if (StrUtil.isBlank(day) || entity == null) {
            return null;
        }
        String index = getIndex(day);
        String type = getType();
        try {
            checkIndex(day);
            checkData(entity);
            return elasticsearchTemplate.save(index, getType(), entity);
        } catch (IOException e) {
            log.error("【ES】添加数据异常！index: {}, type: {}, entity: {}", index, type, JSONUtil.toJsonStr(entity), e);
            return null;
        }
    }

    /**
     * 根据日期动态选择索引并批量更新
     *
     * @param day  日期，格式为：yyyy-MM-dd
     * @param list 待更新的数据
     * @return /
     */
    public boolean saveBatchInDay(String day, Collection<T> list) {
        if (StrUtil.isBlank(day) || CollectionUtil.isEmpty(list)) {
            return false;
        }
        String index = getIndex(day);
        String type = getType();
        try {
            checkIndex(day);
            checkData(list);
            return elasticsearchTemplate.saveBatch(index, type, list);
        } catch (IOException e) {
            log.error("【ES】批量添加数据异常！index: {}, type: {}, size: {}", index, type, list.size(), e);
            return false;
        }
    }

    public void asyncSaveBatchInDay(String day, Collection<T> list) {
        asyncSaveBatchInDay(day, list, DEFAULT_BULK_LISTENER);
    }

    public void asyncSaveBatchInDay(String day, Collection<T> list, ActionListener<BulkResponse> listener) {
        if (StrUtil.isBlank(day) || CollectionUtil.isEmpty(list)) {
            return;
        }
        String index = getIndex(day);
        String type = getType();
        try {
            checkIndex(day);
            checkData(list);
            elasticsearchTemplate.asyncSaveBatch(index, type, list, listener);
        } catch (Exception e) {
            log.error("【ES】异步批量添加数据异常！index: {}, type: {}, size: {}", index, type, list.size(), e);
        }
    }

    public void asyncUpdateBatchIdsInDay(String day, Collection<T> list) {
        asyncUpdateBatchIdsInDay(day, list, DEFAULT_BULK_LISTENER);
    }

    public void asyncUpdateBatchIdsInDay(String day, Collection<T> list, ActionListener<BulkResponse> listener) {
        if (StrUtil.isBlank(day) || CollectionUtil.isEmpty(list)) {
            return;
        }
        String index = getIndex(day);
        String type = getType();
        try {
            checkData(list);
            elasticsearchTemplate.asyncUpdateBatchIds(index, type, list, listener);
        } catch (Exception e) {
            log.error("【ES】异步批量更新数据异常！index: {}, type: {}, size: {}", index, type, list.size(), e);
        }
    }

    public boolean deleteByIdInDay(String day, String id) {
        if (StrUtil.isBlank(day) || StrUtil.isBlank(id)) {
            return false;
        }
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.deleteById(index, type, id);
        } catch (IOException e) {
            log.error("【ES】根据ID删除数据异常！index: {}, type: {}, id: {}", index, type, id, e);
            return false;
        }
    }

    public boolean deleteBatchIdsInDay(String day, Collection<String> ids) {
        if (StrUtil.isBlank(day) || CollectionUtil.isEmpty(ids)) {
            return false;
        }
        String index = getIndex(day);
        String type = getType();
        try {
            return elasticsearchTemplate.deleteBatchIds(index, type, ids);
        } catch (IOException e) {
            log.error("【ES】根据ID批量删除数据异常！index: {}, type: {}, ids: {}", index, type, ids, e);
            return false;
        }
    }

    protected String checkIndex(String day) {
        if (!enableAutoCreateIndex()) {
            return getIndex(day);
        }
        String index = createIndexIfNotExistsInDay(day);
        if (StrUtil.isBlank(index)) {
            String msg = StrUtil.format("【ES】索引 {}_{} 找不到且创建失败！", getAlias(), day);
            throw new DefaultException(ResultCode.ERROR, msg);
        }
        return index;
    }

}

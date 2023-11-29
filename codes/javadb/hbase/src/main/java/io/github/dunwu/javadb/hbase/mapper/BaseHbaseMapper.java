package io.github.dunwu.javadb.hbase.mapper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.hbase.HbaseTemplate;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseEntity;
import io.github.dunwu.javadb.hbase.entity.common.ScrollData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Connection;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * HBase Mapper 基础类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseHbaseMapper<T extends BaseHbaseEntity> implements HbaseMapper<T> {

    protected final HbaseTemplate hbaseTemplate;

    @Override
    public Connection getClient() {
        return hbaseTemplate.getConnection();
    }

    @Override
    public String getNamespace() {
        return "default";
    }

    @Override
    public String getFamily() {
        return "f";
    }

    @Override
    public int deleteById(Serializable id) {

        String rowKey = getIdStr(id);
        if (StrUtil.isBlank(rowKey)) {
            return 0;
        }

        try {
            hbaseTemplate.delete(getFullTableName(), rowKey);
            return 1;
        } catch (IOException e) {
            log.error("【Hbase】deleteById 异常", e);
            return 0;
        }
    }

    @Override
    public int deleteById(T entity) {
        if (entity == null) {
            return 0;
        }
        return deleteById(entity.getRowKey());
    }

    @Override
    public int deleteBatchById(Collection<? extends Serializable> ids) {

        if (CollectionUtil.isEmpty(ids)) {
            return 0;
        }

        List<String> rowKeys = getIdStrList(ids);
        try {
            hbaseTemplate.batchDelete(getFullTableName(), rowKeys.toArray(new String[0]));
            return rowKeys.size();
        } catch (IOException | InterruptedException e) {
            log.error("【Hbase】deleteBatchIds 异常", e);
            return 0;
        }
    }

    @Override
    public int save(T entity) {
        try {
            String rowKey = entity.getRowKey();
            hbaseTemplate.put(getFullTableName(), rowKey, getFamily(), entity);
            return 1;
        } catch (IOException e) {
            log.error("【Hbase】updateById 异常", e);
            return 0;
        }
    }

    @Override
    public int batchSave(Collection<T> list) {

        if (CollectionUtil.isEmpty(list)) {
            return 0;
        }

        try {
            hbaseTemplate.batchPut(getFullTableName(), getFamily(), list);
            return list.size();
        } catch (IOException | InterruptedException e) {
            log.error("【Hbase】batchSave 异常", e);
            return 0;
        }
    }

    @Override
    public T getOneById(Serializable id) {

        String rowKey = getIdStr(id);
        if (StrUtil.isBlank(rowKey)) {
            return null;
        }

        try {
            return hbaseTemplate.getEntity(getFullTableName(), rowKey, getFamily(), getEntityClass());
        } catch (IOException e) {
            log.error("【Hbase】getOneById 异常", e);
            return null;
        }
    }

    @Override
    public Map<? extends Serializable, T> getMapByIds(Collection<? extends Serializable> ids) {

        if (CollectionUtil.isEmpty(ids)) {
            return new LinkedHashMap<>(0);
        }

        List<String> rowKeys = getIdStrList(ids);
        try {
            return hbaseTemplate.getEntityMap(getFullTableName(), rowKeys.toArray(new String[0]), getFamily(),
                getEntityClass());
        } catch (IOException e) {
            log.error("【Hbase】getMapByIds 异常", e);
            return new LinkedHashMap<>(0);
        }
    }

    @Override
    public List<T> scroll(Serializable scrollId, int size) {
        String scrollRowKey = getIdStr(scrollId);
        try {
            ScrollData<T> scrollData =
                hbaseTemplate.getEntityScroll(getFullTableName(), getFamily(), scrollRowKey, size, getEntityClass());
            if (scrollData == null || CollectionUtil.isEmpty(scrollData.getContent())) {
                return new ArrayList<>();
            }
            return new ArrayList<>(scrollData.getContent());
        } catch (IOException e) {
            log.error("【Hbase】getEntityScroll 异常", e);
            return new ArrayList<>();
        }
    }

    protected String getFullTableName() {
        return StrUtil.format("{}:{}", getNamespace(), getTableName());
    }

    protected String getIdStr(Serializable id) {

        if (id == null) {
            return null;
        }

        String rowKey;
        if (id instanceof String) {
            rowKey = (String) id;
        } else {
            rowKey = String.valueOf(id);
        }
        return rowKey;
    }

    protected List<String> getIdStrList(Collection<? extends Serializable> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return new ArrayList<>(0);
        }
        return ids.stream().map(this::getIdStr).filter(Objects::nonNull).collect(Collectors.toList());
    }

}

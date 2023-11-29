package io.github.dunwu.javadb.hbase.mapper;

import cn.hutool.core.collection.CollectionUtil;
import io.github.dunwu.javadb.hbase.annotation.RowKeyUtil;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseContentEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hbase Content Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public interface HbaseUkMapper<T extends BaseHbaseContentEntity> extends HbaseMapper<T>, CommonUkMapper<T> {

    @Override
    default int deleteByUk(Serializable uk) {
        String rowKey = RowKeyUtil.getRowKeyForBucket((String) uk, getEntityClass());
        return deleteById(rowKey);
    }

    @Override
    default int deleteByUk(T entity) {
        String rowKey = RowKeyUtil.getRowKeyForBucket(entity.getUk(), getEntityClass());
        return deleteById(rowKey);
    }

    @Override
    default int deleteBatchByUk(Collection<? extends Serializable> ukList) {
        if (CollectionUtil.isEmpty(ukList)) {
            return 0;
        }
        List<String> rowKeys = ukList.stream()
                                     .map(contentId -> RowKeyUtil.getRowKeyForBucket((String) contentId,
                                         getEntityClass()))
                                     .collect(Collectors.toList());
        return deleteBatchById(rowKeys);
    }

    @Override
    default int updateByUk(T entity) {
        return save(entity);
    }

    @Override
    default int updateBatchByUk(Collection<T> list) {
        return batchSave(list);
    }

    @Override
    default T getOneByUk(Serializable uk) {
        String rowKey = RowKeyUtil.getRowKeyForBucket((String) uk, getEntityClass());
        return getOneById(rowKey);
    }

    @Override
    default List<T> getListByUk(Collection<? extends Serializable> ukList) {
        if (CollectionUtil.isEmpty(ukList)) {
            return new ArrayList<>();
        }

        List<String> rowKeys = ukList.stream()
                                     .map(contentId -> RowKeyUtil.getRowKeyForBucket((String) contentId,
                                         getEntityClass()))
                                     .collect(Collectors.toList());
        return getListByIds(rowKeys);
    }

}

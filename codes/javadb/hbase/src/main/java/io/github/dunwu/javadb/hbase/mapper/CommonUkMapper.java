package io.github.dunwu.javadb.hbase.mapper;

import cn.hutool.core.collection.CollectionUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 基于唯一索引的通用 Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-23
 */
public interface CommonUkMapper<T extends UkGetter> extends CommonMapper<T> {

    /**
     * 根据唯一索引删除
     *
     * @param uk 唯一索引
     */
    default int deleteByUk(Serializable uk) {
        return deleteBatchByUk(Collections.singleton(uk));
    }

    /**
     * 根据唯一索引删除
     *
     * @param entity 实体对象
     */
    int deleteByUk(T entity);

    /**
     * 根据唯一索引批量删除
     *
     * @param ukList 唯一索引列表
     */
    int deleteBatchByUk(Collection<? extends Serializable> ukList);

    /**
     * 根据唯一索引更新
     *
     * @param entity 实体对象
     */
    default int updateByUk(T entity) {
        return updateBatchByUk(Collections.singleton(entity));
    }

    /**
     * 根据唯一索引批量更新
     *
     * @param list 实体对象
     */
    int updateBatchByUk(Collection<T> list);

    /**
     * 根据唯一索引查询
     *
     * @param uk 唯一索引
     */
    default T getOneByUk(Serializable uk) {
        List<T> list = getListByUk(Collections.singleton(uk));
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 根据唯一索引批量查询
     *
     * @param ukList 唯一索引列表
     */
    List<T> getListByUk(Collection<? extends Serializable> ukList);

}

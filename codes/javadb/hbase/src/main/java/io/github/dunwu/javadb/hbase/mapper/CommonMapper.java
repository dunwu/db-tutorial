package io.github.dunwu.javadb.hbase.mapper;

import cn.hutool.core.collection.CollectionUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 通用 Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-22
 */
public interface CommonMapper<T> {

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     */
    default int insert(T entity) {
        return insertBatch(Collections.singleton(entity));
    }

    /**
     * 批量插入记录
     *
     * @param list 实体对象列表
     */
    int insertBatch(Collection<T> list);

    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    default int deleteById(Serializable id) {
        return deleteBatchById(Collections.singleton(id));
    }

    /**
     * 根据实体(ID)删除
     *
     * @param entity 实体对象
     */
    int deleteById(T entity);

    /**
     * 删除（根据ID或实体 批量删除）
     *
     * @param idList 主键ID列表或实体列表(不能为 null 以及 empty)
     */
    int deleteBatchById(Collection<? extends Serializable> idList);

    /**
     * 根据 ID 更新
     *
     * @param entity 实体对象
     */
    default int updateById(T entity) {
        return updateBatchById(Collections.singleton(entity));
    }

    /**
     * 批量更新记录
     *
     * @param list 实体对象列表
     */
    int updateBatchById(Collection<T> list);

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    default T getOneById(Serializable id) {
        List<T> list = getListByIds(Collections.singleton(id));
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    List<T> getListByIds(Collection<? extends Serializable> idList);

}
